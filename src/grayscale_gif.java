import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sun.imageio.plugins.gif.GIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;

public class grayscale_gif {
	// read original color movie(.gif) and convert to grayscale movie(.gif)
	public static void main(String str[]) {

		// movie information
		int movieHeight, movieWidth;
		String movieFilename, moviePath;
		float originalR[][], originalG[][], originalB[][];
		float originalY[][];

		JFileChooser chooser;
		BufferedImage originalMovie, grayscaleMovie;

		GIFImageReader readerGif;

		try {
			// read movie.gif
			// get movie height, width, path, filename and RGB
			System.out.print("Please select a movie(.gif): \n");
			chooser = new JFileChooser();
			chooser.setAcceptAllFileFilterUsed(false);// limit extension .bmp
			chooser.addChoosableFileFilter(new FileNameExtensionFilter("GIF(*.gif)", "gif"));

			// set filename and path
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				moviePath = chooser.getSelectedFile().getAbsolutePath();
				movieFilename = chooser.getSelectedFile().getName();

				// set path, height and width
				originalMovie = ImageIO.read(new File(moviePath));
				movieHeight = originalMovie.getHeight();
				movieWidth = originalMovie.getWidth();

				// get pixel value
				BufferedImage bufferImage;
				readerGif = (GIFImageReader) new GIFImageReaderSpi().createReaderInstance();
				readerGif.setInput(new FileImageInputStream(new File(moviePath)));

				originalR = new float[movieHeight][movieWidth];
				originalG = new float[movieHeight][movieWidth];
				originalB = new float[movieHeight][movieWidth];
				originalY = new float[movieHeight][movieWidth]; // grayscale pixel
				int[][] pixel = new int[movieHeight][movieWidth]; // pixel buffer
				for (int i = 0; i < readerGif.getNumImages(true); i++) {
					bufferImage = readerGif.read(i);
					for (int j = 0; j < readerGif.getHeight(i); j++)
						for (int k = 0; k < readerGif.getWidth(i); k++) {
							// get RGB
							pixel[j][k] = bufferImage.getRGB(k, j);
							originalR[j][k] = (pixel[j][k] >> 16) & 0xFF;
							originalG[j][k] = (pixel[j][k] >> 8) & 0xFF;
							originalB[j][k] = (pixel[j][k] >> 0) & 0xFF;

							// get Y(grayscale, intensity)
							originalY[j][k] = (float) (originalR[j][k] * 0.257 + originalG[j][k] * 0.504
									+ originalB[j][k] * 0.098 + 16);
						}

					// create a folder
					new File(moviePath.substring(0, moviePath.length() - movieFilename.length())
							+ movieFilename.substring(0, movieFilename.length() - 4) + "_frames").mkdir();

					// store grayscale movie
					grayscaleMovie = new BufferedImage(movieWidth, movieHeight, BufferedImage.TYPE_INT_RGB);
					File grayscaleFile = new File(moviePath.substring(0, moviePath.length() - movieFilename.length())
							+ movieFilename.substring(0, movieFilename.length() - 4) + "_frames/"
							+ movieFilename.substring(0, movieFilename.length() - 4) + "_grayscale_" + (i + 1)
							+ ".bmp");
					File colorFile = new File(moviePath.substring(0, moviePath.length() - movieFilename.length())
							+ movieFilename.substring(0, movieFilename.length() - 4) + "_frames/"
							+ movieFilename.substring(0, movieFilename.length() - 4) + "_color_" + (i + 1) + ".bmp");

					for (int j = 0; j < movieHeight; j++)
						for (int k = 0; k < movieWidth; k++)
							grayscaleMovie.setRGB(k, j, ((int) originalY[j][k] << 16) | ((int) originalY[j][k] << 8)
									| ((int) originalY[j][k]));

					ImageIO.write(grayscaleMovie, "GIF", grayscaleFile);
					ImageIO.write(grayscaleMovie, "GIF", colorFile);
				}
			} else
				System.out.println("Something error.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}