/** 
 * Copyright (c) 2022 European Space Agency
 * This file is part of FDS
 * FDS is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * FDS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with FDS. If not, see <https://www.gnu.org/licenses/>.
 **/

package sample;


import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

/**
 *
 * @author ibellas Achlatis Stefanos Christofidi Georgia
 * @version $Id: OneDGaussianFunction.java 23314 2022-05-29 12:20:04Z ibellas $
 */
public class OneDGaussianFunction {
    // Member variables
    int data_size; // width of data

    /**
     *
     * @param data_size
     */
    public OneDGaussianFunction(int data_size) {
        this.data_size = data_size;
    }

    /**
     * Function creator to provide the evaluated values of the 1D-Gaussian model function.
     *
     * @return
     */
    public MultivariateVectorFunction retMVF() {
        return new MultivariateVectorFunction() {

            @Override
            public double[] value(double[] v)
                    throws IllegalArgumentException {
                double[] values = new double[data_size];

                // The parameters
                double ampl = v[0]; 				// amplitude value
                double offset = v[1];				// offset value
                double meanX = v[2];				// mean value of x
                double sigmaX = v[3];				// sigma value of x

                for (int i = 0; i < values.length; ++i) {
                    values[i] =
                            ampl * Math.exp(-Math.pow((i-meanX), 2) / (2 * Math.pow(sigmaX, 2))) + offset;
                }
                return values;
            }
        };
    }


    /**
     * Function creator to provide the jacobian of the 1G_Gaussian model function.
     *
     * Partial differentiations were calculated by using CESGA-Maxima
     *
     * @return	return the jacobian
     */
    public MultivariateMatrixFunction retMMF() {
        return new MultivariateMatrixFunction() {

            @Override
            public double[][] value(double[] point)
                    throws IllegalArgumentException {
                return jacobian(point);
            }

            private double[][] jacobian(double[] v) {
                double[][] jacobian = new double[data_size][4];

                // The parameters
                double ampl = v[0]; 				// amplitude value
                double offset = v[1];				// offset value
                double meanX = v[2];				// mean value of x
                double sigmaX = v[3];				// sigma value of x

                double sgmX2 = sigmaX * sigmaX;
                double sgmX3 = sgmX2 * sigmaX;
                double sgm2X2 = 2 * sgmX2;

                double difX = 0;
                double dfX2 = 0;
                double theExp = 0;
                for (int i = 0; i < jacobian.length; ++i) {
                    difX = i - meanX;
                    dfX2 = difX * difX;
                    theExp = Math.exp(-dfX2/sgm2X2);
                    jacobian[i][0] = theExp;
                    jacobian[i][1] = 1;
                    jacobian[i][2] = ampl * difX * theExp / sgmX2;
                    jacobian[i][3] = ampl * dfX2 * theExp / sgmX3;
                }

                return jacobian;
            }
        };
    }


}
