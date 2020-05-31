package servent.handler;

import app.AppConfig;
import app.Point;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ResultMessage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ResultHandler implements MessageHandler {
    private Message clientMessage;

    public ResultHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        try {
            this.handle();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void handle() {
        if (clientMessage.getMessageType() != MessageType.RESULT) {
            AppConfig.timestampedErrorPrint("Handler got a message that is not RESULT");
            return;
        }

        ResultMessage resultMessage = (ResultMessage) clientMessage;
        Set<Point> resultPoints = resultMessage.getResultPoints();

        String jobName = AppConfig.chordState.getJobRunner().getJobName();
        int width = AppConfig.chordState.getJobRunner().getWidth();
        int height = AppConfig.chordState.getJobRunner().getHeight();
        double proportion = AppConfig.chordState.getJobRunner().getProportion();


        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster writableRaster = image.getRaster();
        int[] rgb = new int[3];
        rgb[0] = 255;
        rgb[1] = 255;
        rgb[2] = 255;
        for (Point p : resultPoints) {
            writableRaster.setPixel(p.getX(), p.getY(), rgb);
        }
        BufferedImage newImage = new BufferedImage(writableRaster.getWidth(), writableRaster.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        newImage.setData(writableRaster);
        try {
            ImageIO.write(newImage, "PNG", new File("fractals/" + jobName + "_" + proportion + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
