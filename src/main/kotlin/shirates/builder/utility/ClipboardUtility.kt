package shirates.builder.utility

import javafx.scene.image.Image
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

object ClipboardUtility {

    fun setImage(bufferedImage: BufferedImage) {

        val clipboard = Clipboard.getSystemClipboard()
        val content = ClipboardContent()
        content.putImage(Image(bufferedImage.toPngInputStream()))
        clipboard.setContent(content)
    }

    fun BufferedImage.setToClipboard(): BufferedImage {

        setImage(this)
        return this
    }

    fun BufferedImage.toPngInputStream(): ByteArrayInputStream {

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(this, "png", outputStream)
        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        return inputStream
    }
}