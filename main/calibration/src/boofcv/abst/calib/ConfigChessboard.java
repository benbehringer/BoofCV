/*
 * Copyright (c) 2011-2015, Peter Abeles. All Rights Reserved.
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

package boofcv.abst.calib;

import boofcv.alg.feature.detect.chess.DetectChessboardFiducial;
import boofcv.factory.filter.binary.ConfigThreshold;
import boofcv.factory.filter.binary.ThresholdType;
import boofcv.factory.shape.ConfigPolygonDetector;
import boofcv.factory.shape.ConfigRefinePolygonCornersToImage;
import boofcv.factory.shape.ConfigRefinePolygonLineToImage;
import boofcv.struct.Configuration;

/**
 * Calibration parameters for chessboard style calibration grid.
 *
 * @see DetectChessboardFiducial
 *
 * @author Peter Abeles
 */
public class ConfigChessboard implements Configuration {
	/**
	 * Number of squares wide the grid is. Target dependent.
	 */
	public int numCols = -1;
	/**
	 * Number of squares tall the grid is. Target dependent.
	 */
	public int numRows = -1;

	/**
	 * The maximum distance in pixels that two corners can be from each other.  In well focused image
	 * this number can be only a few pixels.  The default value has been selected to handle blurred images
	 */
	public double maximumCornerDistance = 8;

	/**
	 * Configuration for thresholding the image
	 */
	public ConfigThreshold thresholding = ConfigThreshold.local(ThresholdType.LOCAL_SQUARE,20);

	/**
	 * Configuration for square detector
	 */
	public ConfigPolygonDetector square = new ConfigPolygonDetector(true, 4,4);

	/**
	 * If true then it only refines the corner region.  Otherwise it will refine the entire line.
	 */
	public boolean refineWithCorners = false;

	/**
	 * Configuration for refining with lines.  Ignored if not used.
	 */
	public ConfigRefinePolygonLineToImage configRefineLines = new ConfigRefinePolygonLineToImage();

	/**
	 * Configuration for refining with corners.  Ignored if not used.
	 */
	public ConfigRefinePolygonCornersToImage configRefineCorners = new ConfigRefinePolygonCornersToImage();

	/**
	 * Physical width of each square on the calibration target
	 */
	public double squareWidth;

	{
		// it erodes the original shape meaning it has to move a greater distance
//		configRefineCorners.maxCornerChangePixel = 5; // TODO reduce

		square.contour2Poly_splitFraction = 0.1;
		square.contour2Poly_minimumSideFraction = 0.1;
		square.minContourImageWidthFraction = 0.0005;

//		square.minimumEdgeIntensity = 0.1;// TODO remove

		// good value for squares.  Set it here to make it not coupled to default values
		configRefineCorners.cornerOffset = 1;
		configRefineCorners.lineSamples = 10;
		configRefineCorners.convergeTolPixels = 0.2;
		configRefineCorners.maxIterations = 5;

		// defaults for if the user toggles it to lines
		configRefineLines.cornerOffset = 1;
		configRefineLines.lineSamples = 10;
		configRefineLines.convergeTolPixels = 0.2;
		configRefineLines.maxIterations = 5;
	}

	public ConfigChessboard(int numCols, int numRows, double squareWidth ) {
		this.numCols = numCols;
		this.numRows = numRows;
		this.squareWidth = squareWidth;
	}


	@Override
	public void checkValidity() {
		if( numCols <= 0 || numRows <= 0 )
			throw new IllegalArgumentException("Must specify then number of rows and columns in the target");
	}
}
