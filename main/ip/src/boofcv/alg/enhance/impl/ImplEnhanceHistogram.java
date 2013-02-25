/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.enhance.impl;

import boofcv.struct.image.*;

/**
 * <p>
 * Functions for enhancing images using the image histogram.
 * </p>
 *
 * <p>
 * NOTE: Do not modify.  Automatically generated by {@link GenerateImplEnhanceHistogram}.
 * </p>
 *
 * @author Peter Abeles
 */
public class ImplEnhanceHistogram {

	public static void applyTransform( ImageUInt8 input , int transform[] , ImageUInt8 output ) {
		for( int i = 0; i < input.height; i++ ) {
			int indexInput = input.startIndex + i*input.stride;
			int indexOutput = output.startIndex + i*output.stride;

			for( int j = 0; j < input.width; j++ ) {
				output.data[indexOutput++] = (byte)transform[input.data[indexInput++] & 0xFF];
			}
		}
	}

	public static void applyTransform( ImageUInt16 input , int transform[] , ImageUInt16 output ) {
		for( int i = 0; i < input.height; i++ ) {
			int indexInput = input.startIndex + i*input.stride;
			int indexOutput = output.startIndex + i*output.stride;

			for( int j = 0; j < input.width; j++ ) {
				output.data[indexOutput++] = (short)transform[input.data[indexInput++] & 0xFFFF];
			}
		}
	}

	public static void applyTransform( ImageSInt8 input , int transform[] , int minValue , ImageSInt8 output ) {
		for( int i = 0; i < input.height; i++ ) {
			int indexInput = input.startIndex + i*input.stride;
			int indexOutput = output.startIndex + i*output.stride;

			for( int j = 0; j < input.width; j++ ) {
				output.data[indexOutput++] = (byte)(transform[input.data[indexInput++]] + minValue);
			}
		}
	}

	public static void applyTransform( ImageSInt16 input , int transform[] , int minValue , ImageSInt16 output ) {
		for( int i = 0; i < input.height; i++ ) {
			int indexInput = input.startIndex + i*input.stride;
			int indexOutput = output.startIndex + i*output.stride;

			for( int j = 0; j < input.width; j++ ) {
				output.data[indexOutput++] = (short)(transform[input.data[indexInput++]] + minValue);
			}
		}
	}

	public static void applyTransform( ImageSInt32 input , int transform[] , int minValue , ImageSInt32 output ) {
		for( int i = 0; i < input.height; i++ ) {
			int indexInput = input.startIndex + i*input.stride;
			int indexOutput = output.startIndex + i*output.stride;

			for( int j = 0; j < input.width; j++ ) {
				output.data[indexOutput++] = (transform[input.data[indexInput++]] + minValue);
			}
		}
	}

	/**
	 * Inefficiently computes the local histogram, but can handle every possible case for image size and
	 * local region size
	 */
	public static void equalizeLocalNaive( ImageUInt8 input , int radius , ImageUInt8 output ,
										   int histogram[] )
	{
		int width = 2*radius+1;
		int maxValue = histogram.length-1;

		for( int y = 0; y < input.height; y++ ) {
			// make sure it's inside the image bounds
			int y0 = y-radius;
			int y1 = y+radius+1;
			if( y0 < 0 ) {
				y0 = 0; y1 = width;
				if( y1 > input.height )
					y1 = input.height;
			} else if( y1 > input.height ) {
				y1 = input.height;
				y0 = y1 - width;
				if( y0 < 0 )
					y0 = 0;
			}

			// pixel indexes
			int indexIn = input.startIndex + y*input.stride;
			int indexOut = output.startIndex + y*output.stride;

			for( int x = 0; x < input.width; x++ ) {
				// make sure it's inside the image bounds
				int x0 = x-radius;
				int x1 = x+radius+1;
				if( x0 < 0 ) {
					x0 = 0; x1 = width;
					if( x1 > input.width )
						x1 = input.width;
				} else if( x1 > input.width ) {
					x1 = input.width;
					x0 = x1 - width;
					if( x0 < 0 )
						x0 = 0;
				}

				// compute the local histogram
				localHistogram(input,x0,y0,x1,y1,histogram);

				// only need to compute up to the value of the input pixel
				int inputValue =  input.data[indexIn++] & 0xff;
				int sum = 0;
				for( int i = 0; i <= inputValue; i++ ) {
					sum += histogram[i];
				}

				int area = (y1-y0)*(x1-x0);
				output.data[indexOut++] = (byte)((sum*maxValue)/area);
			}
		}
	}

	/**
	 * Performs local histogram equalization just on the inner portion of the image
	 */
	public static void equalizeLocalInner( ImageUInt8 input , int radius , ImageUInt8 output ,
										   int histogram[] ) {

		int width = 2*radius+1;
		int area = width*width;
		int maxValue = histogram.length-1;

		for( int y = radius; y < input.height-radius; y++ ) {
			localHistogram(input,0,y-radius,width,y+radius+1,histogram);

			// compute equalized pixel value using the local histogram
			int inputValue = input.unsafe_get(radius, y);
			int sum = 0;
			for( int i = 0; i <= inputValue; i++ ) {
				sum += histogram[i];
			}

			output.set(radius,y, (sum*maxValue)/area );

			// start of old and new columns in histogram region
			int indexOld = input.startIndex + y*input.stride;
			int indexNew = indexOld+width;

			// index of pixel being examined
			int indexIn = input.startIndex + y*input.stride+radius+1;
			int indexOut = output.startIndex + y*output.stride+radius+1;

			for( int x = radius+1; x < input.width-radius; x++ ) {

				// update local histogram by removing the left column
				for( int i = -radius; i <= radius; i++ ) {
					histogram[input.data[indexOld + i*input.stride] & 0xFF]--;
				}

				// update local histogram by adding the right column
				for( int i = -radius; i <= radius; i++ ) {
					histogram[input.data[indexNew + i*input.stride] & 0xFF]++;
				}

				// compute equalized pixel value using the local histogram
				inputValue =  input.data[indexIn++] & 0xff;
				sum = 0;
				for( int i = 0; i <= inputValue; i++ ) {
					sum += histogram[i];
				}

				output.data[indexOut++] = (byte)((sum*maxValue)/area);

				indexOld++;
				indexNew++;
			}
		}
	}

	/**
	 * Local equalization along a row.  Image must be at least the histogram's width (2*r+1) in width and height.
	 */
	public static void equalizeLocalRow( ImageUInt8 input , int radius , int startY , ImageUInt8 output ,
										 int histogram[] , int transform[] ) {

		int width = 2*radius+1;
		int area = width*width;
		int maxValue = histogram.length-1;

		// specify the top and bottom of the histogram window and make sure it is inside bounds
		int hist0 = startY;
		int hist1 = startY+width;
		if( hist1 > input.height ) {
			hist1 = input.height;
			hist0 = hist1 - width;
		}

		// the upper and lower bounds of the region being equalized
		int region0 = startY;
		int region1 = startY+radius;

		// local histogram and transformation
		localHistogram(input,0,hist0,width,hist1,histogram);

		int sum = 0;
		for( int i = 0; i < histogram.length; i++ ) {
			transform[i] = sum += histogram[i];
		}

		// equalize the first square region
		for( int y = region0; y < region1; y++ ) {
			int indexIn = input.startIndex + y*input.stride;
			int indexOut = output.startIndex + y*output.stride;

			for( int x = 0; x <= radius; x++ ) {
				int inputValue =  input.data[indexIn++] & 0xff;
				output.data[indexOut++] = (byte)((transform[ inputValue ]*maxValue)/area);
			}
		}

		// move right while equalizing the columns one at a time
		for( int x = radius+1; x < input.width-radius-1; x++ ) {

			// remove the left most column
			int indexIn = input.startIndex + x-radius-1;
			for( int y = hist0; y < hist1; y++ ) {
				histogram[input.data[indexIn + y*input.stride] & 0xFF]--;
			}
			// add the right most column
			indexIn += width;
			for( int y = hist0; y < hist1; y++ ) {
				histogram[input.data[indexIn + y*input.stride] & 0xFF]++;
			}

			// compute transformation table
			sum = 0;
			for( int i = 0; i < histogram.length; i++ ) {
				transform[i] = sum += histogram[i];
			}

			// compute the output down the column
			indexIn = input.startIndex + region0*input.stride + x;
			int indexOut = output.startIndex + region0*output.stride + x;
			for( int y = 0; y < radius; y++ ) {
				int inputValue =  input.data[indexIn] & 0xff;
				output.data[indexOut] = (byte)((transform[ inputValue ]*maxValue)/area);

				indexIn += input.stride;
				indexOut += output.stride;
			}
		}

		// equalize the final square region
		localHistogram(input,input.width-width,hist0,input.width,hist1,histogram);

		sum = 0;
		for( int i = 0; i < histogram.length; i++ ) {
			transform[i] = sum += histogram[i];
		}

		for( int y = region0; y < region1; y++ ) {
			int x = input.width-radius-1;

			int indexIn = input.startIndex + y*input.stride + x;
			int indexOut = output.startIndex + y*output.stride + x;

			for( ; x < input.width; x++ ) {
				int inputValue =  input.data[indexIn++] & 0xff;
				output.data[indexOut++] = (byte)((transform[ inputValue ]*maxValue)/area);
			}
		}
	}

	/**
	 * Local equalization along a column.  Image must be at least the histogram's width (2*r+1) in width and height.
	 */
	public static void equalizeLocalCol( ImageUInt8 input , int radius , int startX , ImageUInt8 output ,
										 int histogram[] , int transform[] ) {

		int width = 2*radius+1;
		int area = width*width;
		int maxValue = histogram.length-1;

		// specify the top and bottom of the histogram window and make sure it is inside bounds
		int hist0 = startX;
		int hist1 = startX+width;
		if( hist1 > input.width ) {
			hist1 = input.width;
			hist0 = hist1 - width;
		}

		// initialize the histogram.  ignore top border
		localHistogram(input,hist0,0,hist1,width,histogram);

		// compute transformation table
		int sum = 0;
		for( int i = 0; i < histogram.length; i++ ) {
			transform[i] = sum += histogram[i];
		}

		// compute the output across the row
		int indexIn = input.startIndex + radius*input.stride + startX;
		int indexOut = output.startIndex + radius*output.stride + startX;
		for( int x = 0; x < radius; x++ ) {
			int inputValue =  input.data[indexIn++] & 0xff;
			output.data[indexOut++] = (byte)((transform[ inputValue ]*maxValue)/area);
		}

		// move down while equalizing the rows one at a time
		for( int y = radius+1; y < input.height-radius; y++ ) {

			// remove the top most row
			indexIn = input.startIndex + (y-radius-1)*input.stride;
			for( int x = hist0; x < hist1; x++ ) {
				histogram[input.data[indexIn + x] & 0xFF]--;
			}
			// add the bottom most row
			indexIn += width*input.stride;
			for( int x = hist0; x < hist1; x++ ) {
				histogram[input.data[indexIn + x] & 0xFF]++;
			}

			// compute transformation table
			sum = 0;
			for( int i = 0; i < histogram.length; i++ ) {
				transform[i] = sum += histogram[i];
			}

			// compute the output across the row
			indexIn = input.startIndex + y*input.stride + startX;
			indexOut = output.startIndex + y*output.stride + startX;
			for( int x = 0; x < radius; x++ ) {
				int inputValue =  input.data[indexIn++] & 0xff;
				output.data[indexOut++] = (byte)((transform[ inputValue ]*maxValue)/area);
			}
		}
	}

	/**
	 * Computes the local histogram just for the specified inner region
	 */
	public static void localHistogram( ImageUInt8 input , int x0 , int y0 , int x1, int y1 , int histogram[] ) {
		for( int i = 0; i < histogram.length; i++ )
			histogram[i] = 0;

		for( int i = y0; i < y1; i++ ) {
			int index = input.startIndex + i*input.stride + x0;
			int end = index + x1-x0;
			for( ; index < end; index++ ) {
				histogram[input.data[index] & 0xFF]++;
			}
		}
	}


}
