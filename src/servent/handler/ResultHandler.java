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

        String jobName = resultMessage.getJobDetails().getName();
        int width = resultMessage.getJobDetails().getWidth();
        int height = resultMessage.getJobDetails().getHeight();
        double proportion = resultMessage.getJobDetails().getProportion();


        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster writableRaster = image.getRaster();
        int[] rgb = new int[3];
        rgb[0] = 255;
        rgb[1] = 255;
        rgb[2] = 255;
        for (Point p : resultPoints) {
            try {
                writableRaster.setPixel(p.getX(), p.getY(), rgb);
            } catch (ArrayIndexOutOfBoundsException exception) {
                AppConfig.timestampedErrorPrint(exception.getMessage() + ": x=" + p.getX() + ", y=" + p.getY());
            }
        }
        BufferedImage newImage = new BufferedImage(writableRaster.getWidth(), writableRaster.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        newImage.setData(writableRaster);
        try {
            String fileName = jobName + "_" + proportion;
            if (resultMessage.hasFractalId()) {
                fileName = fileName + "_" + resultMessage.getFractalId();
            }
            ImageIO.write(newImage, "PNG", new File("fractals/" + fileName + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
