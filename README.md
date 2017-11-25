# SmartAvatarCrop
android project write in kotlin, pure kotlin code support picture scale, translation, mirror and then crop certain area, because all this enabled by androdi matrix and canvas, so crop is really fast more implementable to your project.
# Start
works are down by SquareCropView, in your activity ,just add this view to your layout access it in your activity code.
//start crop by set your bitmap resource.
squareCropView.startCropProcess(yourBitmap)
then you can scale or translate picture shows on screen, picture will never scroll out of 
the rectangle.
//rotate picture by certain degree clockwise
squareCropView.rotate(degree)
//mirror picture horizontal
squareCropView.flip()
//apply crop, after all option you set to this picture, return crop result
squareCropView.applyCrop()
# Conclusion
Try it and be pleasant with this small sample project,or you can do anything to benefit yourself.
Any suggestion is wellcome.
