import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

public class colorization {
	// image information
	static int imageHeight, imageWidth;
	static String imageFilename, imagePath;
	static float grayscaleR[][], grayscaleG[][], grayscaleB[][];
	static float grayscaleY[][], grayscaleU[][], grayscaleV[][];
	static float colorR[][], colorG[][], colorB[][];
	static float colorY[][], colorU[][], colorV[][];
	static float resultR[][], resultG[][], resultB[][];
	static float resultY[][], resultU[][], resultV[][];
	static float differenceU[][]; // colorU-grayscaleU
	static double[] weight;
	// static float b_u[];
	// static float b_v[];

	public static void main(String str[]) {

		JFileChooser chooser;
		BufferedImage grayscaleImage, colorImage, resultImage;

		try {
			// read original grayscale image.bmp
			System.out.println("Please select an original grayscale image(.bmp): \n");
			chooser = new JFileChooser();
			chooser.setAcceptAllFileFilterUsed(false); // limit extension .bmp
			chooser.addChoosableFileFilter(new FileNameExtensionFilter("BMP(*.bmp)", "bmp"));

			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				imagePath = chooser.getSelectedFile().getAbsolutePath();
				imageFilename = chooser.getSelectedFile().getName();
				System.out.println(imagePath.substring(0, imagePath.length() - imageFilename.length())); // path

				// set path, height and width
				grayscaleImage = ImageIO.read(new File(imagePath));
				imageHeight = grayscaleImage.getHeight();
				imageWidth = grayscaleImage.getWidth();

				// get pixel value
				grayscaleR = new float[imageHeight][imageWidth];
				grayscaleG = new float[imageHeight][imageWidth];
				grayscaleB = new float[imageHeight][imageWidth];
				grayscaleY = new float[imageHeight][imageWidth]; // grayscale pixel
				grayscaleU = new float[imageHeight][imageWidth];
				grayscaleV = new float[imageHeight][imageWidth];
				int[][] pixel = new int[imageHeight][imageWidth]; // pixel buffer
				for (int i = 0; i < imageHeight; i++)
					for (int j = 0; j < imageWidth; j++) {
						// get RGB
						pixel[i][j] = grayscaleImage.getRGB(j, i);
						grayscaleR[i][j] = (pixel[i][j] >> 16) & 0xFF;
						grayscaleG[i][j] = (pixel[i][j] >> 8) & 0xFF;
						grayscaleB[i][j] = (pixel[i][j] >> 0) & 0xFF;

						// get YUV
						grayscaleY[i][j] = (float) (grayscaleR[i][j] * 0.257 + grayscaleG[i][j] * 0.504
								+ grayscaleB[i][j] * 0.098 + 16);
						grayscaleU[i][j] = (float) (grayscaleR[i][j] * -0.148 + grayscaleG[i][j] * -0.291
								+ grayscaleB[i][j] * 0.439 + 128);
						grayscaleV[i][j] = (float) (grayscaleR[i][j] * 0.439 + grayscaleG[i][j] * -0.368
								+ grayscaleB[i][j] * -0.071 + 128);
					}
			} else
				System.out.println("Something error(grayscale).");

			// read color reference image.bmp
			System.out.println("Please select a color reference image(.bmp): \n");
			chooser = new JFileChooser();
			chooser.setAcceptAllFileFilterUsed(false); // limit extension .bmp
			chooser.addChoosableFileFilter(new FileNameExtensionFilter("BMP(*.bmp)", "bmp"));

			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				imagePath = chooser.getSelectedFile().getAbsolutePath();
				imageFilename = chooser.getSelectedFile().getName();
				// System.out.println(imagePath.substring(0, imagePath.length() -
				// imageFilename.length())); // path

				// set path, height and width
				colorImage = ImageIO.read(new File(imagePath));
				imageHeight = colorImage.getHeight();
				imageWidth = colorImage.getWidth();

				// get pixel value
				colorR = new float[imageHeight][imageWidth];
				colorG = new float[imageHeight][imageWidth];
				colorB = new float[imageHeight][imageWidth];
				colorY = new float[imageHeight][imageWidth]; // grayscale pixel
				colorU = new float[imageHeight][imageWidth];
				colorV = new float[imageHeight][imageWidth];
				int[][] pixel = new int[imageHeight][imageWidth]; // pixel buffer
				for (int i = 0; i < imageHeight; i++)
					for (int j = 0; j < imageWidth; j++) {
						// get RGB
						pixel[i][j] = colorImage.getRGB(j, i);
						colorR[i][j] = (pixel[i][j] >> 16) & 0xFF;
						colorG[i][j] = (pixel[i][j] >> 8) & 0xFF;
						colorB[i][j] = (pixel[i][j] >> 0) & 0xFF;

						System.out.println(colorG[i][j]);

						// get YUV
						colorY[i][j] = (float) (colorR[i][j] * 0.257 + colorG[i][j] * 0.504 + colorB[i][j] * 0.098
								+ 16);
						colorU[i][j] = (float) (colorR[i][j] * -0.148 + colorG[i][j] * -0.291 + colorB[i][j] * 0.439
								+ 128);
						colorV[i][j] = (float) (colorR[i][j] * 0.439 + colorG[i][j] * -0.368 + colorB[i][j] * -0.071
								+ 128);
					}
				// --------------- colorization using optimization ---------------

				// sparse matrix
				SparseDoubleMatrix2D A = new SparseDoubleMatrix2D(imageHeight * imageWidth, imageHeight * imageWidth);
				DenseDoubleMatrix2D b_uu = new DenseDoubleMatrix2D(imageHeight * imageWidth, 1);
				DenseDoubleMatrix2D b_vv = new DenseDoubleMatrix2D(imageHeight * imageWidth, 1);

				// compute weight
				// weight = new double[imageHeight*imageWidth][imageHeight*imageWidth];

				// b_u = new float[imageHeight*imageWidth];
				// b_v = new float[imageHeight*imageWidth];

				int x_start = 0;
				int x_end = 0;
				int y_start = 0;
				int y_end = 0;
				double variance = 0;
				double square = 0;
				double sum = 0;
				double count = 0;
				double average = 0;

				for (int i = 0; i < imageHeight; i++)
					for (int j = 0; j < imageWidth; j++) {
						weight = new double[imageHeight * imageWidth];
						if (colorU[i][j] - grayscaleU[i][j] == 0) {
							x_start = Math.max(0, i - 1);
							x_end = Math.min(imageHeight - 1, i + 1);
							y_start = Math.max(0, j - 1);
							y_end = Math.min(imageWidth - 1, j + 1);

							variance = 0;
							square = 0;
							sum = 0;
							count = 0;
							average = 0;
							for (int ii = x_start; ii < x_end + 1; ii++)
								for (int jj = y_start; jj < y_end + 1; jj++) {
									if (ii != i || jj != j) {

										sum += grayscaleY[ii][jj];
										count++;
									}
								}
							average = sum / count;
							for (int ii = x_start; ii < x_end + 1; ii++)
								for (int jj = y_start; jj < y_end + 1; jj++) {
									if (ii != i || jj != j) {
										square += Math.pow(grayscaleY[ii][jj] - average, 2);

									}
								}
							variance = square / count;
							if (variance < 1e-6) {
								variance = 1e-6;
							}
							float weight_sum = 0;
							for (int ii = x_start; ii < x_end + 1; ii++)
								for (int jj = y_start; jj < y_end + 1; jj++) {
									if (ii != i || jj != j) {
										weight[ii * imageWidth + jj] = Math.exp(
												-Math.pow(grayscaleY[i][j] - grayscaleY[ii][jj], 2) / 2 * variance);
										weight_sum += weight[ii * imageWidth + jj];
										if (Double.isNaN(weight_sum)) {
											System.out.println(ii + "," + jj);
											System.out.println("variance" + variance);
										}

									}

								}
							for (int ii = x_start; ii < x_end + 1; ii++)
								for (int jj = y_start; jj < y_end + 1; jj++) {
									if (ii != i || jj != j) {
										// weight[i][ii*imageWidth+jj] = weight[i][ii*imageWidth+jj]/weight_sum;
										A.setQuick(i * imageWidth + j, ii * imageWidth + jj,
												-weight[ii * imageWidth + jj] / weight_sum);
									}

								}
						} else {
							// b_u[i*imageWidth +j] = colorU[i][j];
							b_uu.set(i * imageWidth + j, 0, colorU[i][j]);
							// b_v[i*imageWidth +j] = colorV[i][j];
							b_vv.set(i * imageWidth + j, 0, colorV[i][j]);
						}
						// weight[i*imageWidth+j][i*imageWidth+j] = 1 ;
						A.setQuick(i * imageWidth + j, i * imageWidth + j, 1);
					}
				System.out.println("Start to solve");
				// Solve
				Algebra algebra = new Algebra();
				DoubleMatrix2D r_u = algebra.solve(A, b_uu);
				DoubleMatrix2D r_v = algebra.solve(A, b_vv);
				// The 'x' vector will be sparse
				// Vector r_u = solver.solve(b_uu);
				// System.out.println(r_v);
				// System.out.println(b_uu);

				// Vector r_v = solver.solve(b_vv);
				System.out.println("Get result_v_");

				// compute result pixel
				resultR = new float[imageHeight][imageWidth];
				resultG = new float[imageHeight][imageWidth];
				resultB = new float[imageHeight][imageWidth];
				resultY = new float[imageHeight][imageWidth]; // grayscale pixel
				resultU = new float[imageHeight][imageWidth];
				resultV = new float[imageHeight][imageWidth];

				System.out.println("--------------------------------------------------------");
				for (int i = 0; i < imageHeight; i++)
					for (int j = 0; j < imageWidth; j++) {
						resultY[i][j] = grayscaleY[i][j];
						resultU[i][j] = (float) r_u.get(i * imageWidth + j, 0);
						resultV[i][j] = (float) r_v.get(i * imageWidth + j, 0);
						// resultU[i][j] = colorU[i][j];
						// resultV[i][j] = colorV[i][j];

						resultR[i][j] = normal((float) ((resultY[i][j] - 16) * 1.164 + (resultV[i][j] - 128) * 1.596));
						resultG[i][j] = normal((float) ((resultY[i][j] - 16) * 1.164 + (resultV[i][j] - 128) * -0.831
								+ (resultU[i][j] - 128) * -0.391));
						resultB[i][j] = normal((float) ((resultY[i][j] - 16) * 1.164 + (resultU[i][j] - 128) * 2.018));
					}

				// store result image
				// System.out.println("start to store");
				resultImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
				File resultFile = new File(
						imagePath.substring(0, imagePath.length() - imageFilename.length()) + "result.bmp"); // path+filename

				for (int i = 0; i < imageHeight; i++)
					for (int j = 0; j < imageWidth; j++)
						resultImage.setRGB(j, i,
								((int) resultR[i][j] << 16) | ((int) resultG[i][j] << 8) | ((int) resultB[i][j]));

				ImageIO.write(resultImage, "BMP", resultFile);

			} else
				System.out.println("Something error(color).");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 0-255
	static float normal(float ori) {
		float result = ori;
		if (result > 255)
			result = 255;
		else if (result < 0)
			result = 0;
		return result;
	}
}
