# Himawari Downloader
Himawari Downloader is a Java based application that let's you download sattelite images from the japanese Himawari sattelite.
The imagery is available on this site https://himawari8.nict.go.jp/. However this site has the drawback that you can't simply save the image because it is split into multiple pieces.
The program I wrote loops through and downloads all required images depending on the resolution. They are then stitched together and can be saved to disk.

Attention! 11000px option produces 150+MB images!

![Himawari 8 - 06.09.2018 04:40 UTC](https://i.imgur.com/hUQv41q.png)

# Features
* Preview images before downloading 150MB of images
* Download images in 6 different resolutions
* Save images to disk
* 50+Mbit/s download speed in multithreaded mode (my internet isn't any faster)

# Todo
* [ ] Download an interval of pictures
* [ ] Custom save location and file names
* [ ] Zoomable preview window
* [ ] Update progress bar corresponding to status

# Download
Get the latest version from the releases page: https://github.com/LsHallo/HimawariDownloader/releases
