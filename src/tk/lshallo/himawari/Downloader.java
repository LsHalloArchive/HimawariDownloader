package tk.lshallo.himawari;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/* URLS
 * https://himawari8-dl.nict.go.jp/himawari8/img/D531106/1d/550/2018/06/04/233000_0_0.png
 * https://himawari8-dl.nict.go.jp/himawari8/img/D531106/2d/550/2018/06/04/233000_0_0.png
 * https://himawari8-dl.nict.go.jp/himawari8/img/D531106/4d/550/2018/06/04/233000_0_0.png
 * https://himawari8-dl.nict.go.jp/himawari8/img/D531106/8d/550/2018/06/04/233000_0_0.png
 * https://himawari8-dl.nict.go.jp/himawari8/img/D531106/16d/550/2018/06/04/233000_0_0.png
 * https://himawari8-dl.nict.go.jp/himawari8/img/D531106/20d/550/2018/06/04/233000_0_0.png
 */

public class Downloader extends Thread {
	private int row, num;
	private LocalDateTime dt = LocalDateTime.now();
	private DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy/MM/dd");
	private DateTimeFormatter tf = DateTimeFormatter.ofPattern("HHmmss");
	private Image[] result;
	
	Downloader() {}
	
	Downloader(int row, int num, LocalDateTime dt, String name, ThreadGroup tg) {
		super(tg, name);
		this.row = row;
		this.num = num;
		this.dt = dt;
		this.result = new Image[num];
	}

    /**
     * Run thread of downloader class. Overrides thread run method
     */
	public void run() {
		for(int i = 0; i <  num; i++) {
			try {
				URL url = new URL("https://himawari8-dl.nict.go.jp/himawari8/img/D531106/" + num + "d/550/" + df.format(dt) + "/" + tf.format(dt) + "_" + row + "_" + i + ".png");
				try {
					result[i] = ImageIO.read(url);
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("Thread " + getName() + " has downloaded image " + (i + 1) + "/" + num);
				Himawari.controller.increaseProgress();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Thread " + getName() + " is done!");
	}

	private Image[] getResult() {
		return result;
	}

	private int getRow() {
		return row;
	}

    /**
     * Single threaded download of the image, stitched together
     * @param res number of images vertical and horizontal
     * @param time LocalDateTime object with time
     * @return BufferedImage image result
     */
	BufferedImage single(int res, LocalDateTime time) {
		dt = primeTime(time);
		Image[] src = new Image[res * res];
		
		for(int x = 0; x < res; x++) {
			for(int y = 0; y < res; y++) {
				try {
					URL url = new URL("https://himawari8-dl.nict.go.jp/himawari8/img/D531106/" + res + "d/550/" + df.format(dt) + "/" + tf.format(dt) + "_" + x + "_" + y + ".png");
					src[x * res + y] = ImageIO.read(url);
					Himawari.controller.increaseProgress();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return stitchImage(res, res, src);
	}

    /**
     * Multi threaded download of images
     * Creates width amount of threads
     * @param res number of images vertical and horizontal
     * @param time LocalDateTime object with time
     * @return BufferedImage image result
     */
	BufferedImage multi(int res, LocalDateTime time) {
		dt = primeTime(time);
		Image[] src = new Image[res * res];
		ThreadGroup tg = new ThreadGroup("downloads");
		List<Downloader> downloads = new ArrayList<>();
		
		for(int i = 0; i < res; i++) {
			Downloader d = new Downloader(i, res, dt, i+"", tg);
			downloads.add(d);
			d.start();
		}
		
		while(tg.activeCount() > 0) {
			try {
				System.out.println("Active Threads: " + tg.activeCount());
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for(Downloader d : downloads) {
			int row = d.getRow();
			Image[] result = d.getResult();
			
			for(int l = 0; l < res; l++) {
				System.out.println("Copying image " + l + " from thread " + d.getName() + " to src[" + (row * res + l) + "]");
				src[row * res + l] = result[l];
			}
		}
		
		System.out.println("Computing complete image. Please wait!");
		BufferedImage result = stitchImage(res, res, src);
		System.out.println("Done");
		return result;
	}

    /**
     * Downloads a preview image (lowest resolution available)
     * @param time LocalDateTime object with date and time
     * @return BufferedImage preview image
     */
	BufferedImage preview(LocalDateTime time) {
		dt = time;
		dt = primeTime(dt);
		
		try {
			URL url = new URL("https://himawari8-dl.nict.go.jp/himawari8/img/D531106/1d/550/" + df.format(dt) + "/" + tf.format(dt) + "_0_0.png");
			return ImageIO.read(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * Stitches multiple images together to get one big image
     * @param width number of vertical images that need stitching
     * @param height number of horizontal images that need stitching (probably the same as width)
     * @param imgArr array of images that need to be stitched (length needs to be width * height)
     * @return BufferedImage with following size: width * imgWidth (constant), height * imgHeight (constant)
     */
	private BufferedImage stitchImage(int width, int height, Image[] imgArr) {
		final int imgWidth = 550, imgHeight = 550;

		BufferedImage out = new BufferedImage(imgWidth * width, imgHeight * height, BufferedImage.TYPE_INT_RGB);
		Graphics g = out.getGraphics();
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				g.drawImage(imgArr[x * width +  y], x * imgWidth, y * imgHeight, null);
			}
			System.out.println(new DecimalFormat("#.##%").format((x + .0) / width) + " complete.");
		}
		return out;
	}

	/**
	    Images are only saved every 10 minutes, this method takes any time and adjusts it to the nearest 10th
        @param dtime LocalDateTime object that needs adjustment

        @return adjusted LocalDateTime object
	 */
	private LocalDateTime primeTime(LocalDateTime dtime) {
        dtime = dtime.plusMinutes(distanceToNearestTenth(dtime.getMinute()));
        return dtime;
    }

    /**
     * returns the distance to the nearest 10th
     * result can be positive or negative
     * @param val number to adjust to nearest 10th
     * @return adjusted number
     */
	private int distanceToNearestTenth(int val) {
		return (((val + 5) / 10) * 10) - val;
	}
}
