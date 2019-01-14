import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class grayscale {
	// read original color image(.bmp) and convert to grayscale image(.bmp)
	public static void main(String str[]) {

		// image information
		int imageHeight, imageWidth;
		String imageFilename, imagePath;
		float originalR[][], originalG[][], originalB[][];
		float originalY[][];

		JFileChooser chooser;
		BufferedImage originalImage, grayscaleImage;

		try {
			// read image.bmp
			// get image height, width, path, filename and RGB
			System.out.print("Please select an image(.bmp): \n");
			chooser = new JFileChooser();
			chooser.setAcceptAllFileFilterUsed(false);// limit extension .bmp
			chooser.addChoosableFileFilter(new FileNameExtensionFilter("BMP(*.bmp)", "bmp"));

			// set filename and path
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				imagePath = chooser.getSelectedFile().getAbsolutePath();
				imageFilename = chooser.getSelectedFile().getName();

				// set path, height and width
				originalImage = ImageIO.read(new File(imagePath));
				imageHeight = originalImage.getHeight();
				imageWidth = originalImage.getWidth();

				// get pixel value
				originalR = new float[imageHeight][imageWidth];
				originalG = new float[imageHeight][imageWidth];
				originalB = new float[imageHeight][imageWidth];
				originalY = new float[imageHeight][imageWidth]; // grayscale pixel
				int[][] pixel = new int[imageHeight][imageWidth]; // pixel buffer
				for (int i = 0; i < imageHeight; i++)
					for (int j = 0; j < imageWidth; j++) {
						// get RGB
						pixel[i][j] = originalImage.getRGB(j, i);
						originalR[i][j] = (pixel[i][j] >> 16) & 0xFF;
						originalG[i][j] = (pixel[i][j] >> 8) & 0xFF;
						originalB[i][j] = (pixel[i][j] >> 0) & 0xFF;

						// get Y(grayscale, intensity)
						originalY[i][j] = (float) (originalR[i][j] * 0.257 + originalG[i][j] * 0.504
								+ originalB[i][j] * 0.098 + 16);
					}

				// store grayscale image
				grayscaleImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
				File grayscaleFile = new File(imagePath.substring(0, imagePath.length() - imageFilename.length())
						+ imageFilename.substring(0, imageFilename.length() - 4) + "_grayscale.bmp"); // path + filename
				File colorFile = new File(imagePath.substring(0, imagePath.length() - imageFilename.length())
						+ imageFilename.substring(0, imageFilename.length() - 4) + "_color.bmp");
				for (int i = 0; i < imageHeight; i++)
					for (int j = 0; j < imageWidth; j++)
						grayscaleImage.setRGB(j, i,
								((int) originalY[i][j] << 16) | ((int) originalY[i][j] << 8) | ((int) originalY[i][j]));

				ImageIO.write(grayscaleImage, "BMP", grayscaleFile);
				ImageIO.write(grayscaleImage, "BMP", colorFile);
			} else
				System.out.println("Something error.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}