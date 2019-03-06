
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.*;


public class recognizeText {

    static String uri_customvision = "https://southcentralus.api.cognitive.microsoft.com/customvision/v2.0/Prediction/a2b54b65-78af-4eeb-bbb5-43e75b0138bf/image";
    static String prediction_Key = "825b003a7bef4f598f2fae49af98df6e";
    static String content_Type = "application/octet-stream";
    static String uri = "https://centralindia.api.cognitive.microsoft.com/vision/v2.0/recognizeText?mode=Printed";
    //"https://southcentralus.api.cognitive.microsoft.com/customvision/v2.0/Prediction/a2b54b65-78af-4eeb-bbb5-43e75b0138bf/image";
    static String subscriptionKey = "6a2bb2366ea54a9fb8b2785861d958dd";
    //static String content_Type = "application/octet-stream";

    public static void main(String[] args) throws IOException, URISyntaxException,JSONException {
        String imagepath = "D:\\images/3.jpg";
        try {
            recognition(Files.readAllBytes(Paths.get(imagepath)));
            objectDetection(Files.readAllBytes(Paths.get(imagepath)));


        } catch (JSONException e) {
            e.printStackTrace();
        }

        }
    private static void recognition(final byte[] bytes)throws IOException,JSONException{
        byte[] data1 = Files.readAllBytes(Paths.get("D:\\images/3.jpg"));

        System.out.println(" Number plate Recognition started");

        final ByteArrayInputStream stream = new ByteArrayInputStream(data1);

        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);
        httpPost.setHeader("Content-Type", content_Type);
        InputStreamEntity inputStreamEntity = new InputStreamEntity(stream);
        httpPost.setEntity(inputStreamEntity);
        HttpResponse response = client.execute(httpPost);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "";

        String responseString = new BasicResponseHandler().handleResponse(response);
        //System.out.println("response=" + responseString);


        Header header_required = response.getFirstHeader("Operation-Location");
        String GET_URL = header_required.getValue();
        //System.out.print("required value=" + header_required.getValue());


        String url = GET_URL;

        HttpClient client2 = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        // add request header
        request.addHeader("Ocp-Apim-Subscription-Key", "6a2bb2366ea54a9fb8b2785861d958dd");

        System.out.println("\nWaiting ... begins");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("\nWaiting ... Ends");

        //System.out.println("\nSending 'GET' request to URL : " + url);
        HttpResponse get_response = client2.execute(request);

       // System.out.println("Response Code : " + get_response.getStatusLine().getStatusCode());


        BufferedReader rd2 = new BufferedReader(new InputStreamReader(get_response.getEntity().getContent()));

        String json_string = EntityUtils.toString(get_response.getEntity());
       // System.out.println("OUTPUT GET JSON=" + json_string);
        JSONObject json = new JSONObject(json_string);
        JSONObject recognitionResult = json.getJSONObject("recognitionResult");
        JSONArray lines = recognitionResult.getJSONArray("lines");


        for (int x = 0; x < ((org.json.JSONArray) lines).length(); x++) {
            JSONObject first = lines.getJSONObject(x);
            String text = first.getString("text");
            System.out.println(text);
        }

    }


    private static void objectDetection(final byte[] bytes) throws IOException,JSONException {
        System.out.println("Blurring number plate started");
        final ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri_customvision);
        httpPost.setHeader("Prediction-Key", prediction_Key);
        httpPost.setHeader("Content-Type", content_Type);
        InputStreamEntity inputStreamEntity = new InputStreamEntity(stream);
        httpPost.setEntity(inputStreamEntity);
        HttpResponse response = client.execute(httpPost);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "";

        while ((line = rd.readLine()) != null) {

            //System.out.println("line"+ line);
            org.json.JSONObject root = new JSONObject(line);
            org.json.JSONArray  predictionArray = root.getJSONArray("predictions");

            for(int i=0;i<predictionArray.length();i++)
            {
                JSONObject first = predictionArray.getJSONObject(i);
                // probability = first.getString("probability");
                // System.out.println("probability"+first.getDouble("probability"));
                if((first.getDouble("probability")>0.5)) {
                    String boundingbox = first.getString("boundingBox");
                    // System.out.println("boundingbox="+boundingbox);
                    org.json.JSONObject bb = new JSONObject(boundingbox);
                   /* System.out.println("top=" + bb.getDouble("top"));
                    System.out.println("left=" + bb.getDouble("left"));
                    System.out.println("width=" + bb.getDouble("width"));
                    System.out.println("height=" + bb.getDouble("height"));*/
                    Double h= bb.getDouble("height");
                    Double t=bb.getDouble("top");
                    Double l= bb.getDouble("left");
                    Double w= bb.getDouble("width");
                    drawrectangle(t,l,w,h);


                }
            }
        }
    }
    public static void drawrectangle(double top,double left,double width,double height) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("D:\\images/3.jpg"));
            int heightImage= img.getHeight();
            int widthImage = img.getWidth();
            // System.out.println(heightImage+","+widthImage);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(Color.RED);
            int x1=(int)(left*widthImage);
            int y1=(int) (top*heightImage);
            int w1=(int) (width*widthImage);
            int h1=(int)(height*heightImage);
            g2d.drawRect( x1,y1,w1,h1);
           // System.out.println( x1+" "+y1+" "+w1+" "+h1);
            File outputfile = new File("saved.jpg");
            ImageIO.write(img, "jpg", outputfile);
            System.out.println("Writing complete.");
            //Cropping
            BufferedImage croppedImage = img.getSubimage(x1, y1, w1, h1);
            //File outputfile1 = new File("Cropped.jpg");
            //ImageIO.write(croppedImage, "jpg", outputfile1);
            BufferedImage tempimage = resizeImage(croppedImage, croppedImage.getWidth(), croppedImage.getHeight());
            File outputfile2 = new File("Resized.jpg");
            ImageIO.write(tempimage, "jpg", outputfile2);
            //blurring
            //int radius=8;
            Color c[];
            BufferedImage bi = new BufferedImage(tempimage.getWidth(), tempimage.getHeight(), BufferedImage.TYPE_INT_RGB);
            int i = 0;
            int max = 400, radius = 10;
            int a1 = 0, r1 = 0, g1 = 0, b1 = 0;
            c = new Color[max];
            int x = 1, y = 1, m,n,ex = 5, d = 0;
            for (x = radius; x < tempimage.getHeight() - radius; x++) {
                for (y = radius; y < tempimage.getWidth() - radius; y++) {

                    //20x20 matrix
                    for (m = x - radius; m < x + radius; m++) {
                        for (n = y - radius; n < y + radius; n++) {
                            c[i++] = new Color(tempimage.getRGB(n, m));
                            //System.out.println(i);
                        }
                    }
                    i = 0;

                    for (d = 0; d < max; d++) {
                        a1 = a1 + c[d].getAlpha();
                    }
                    a1 = a1 / (max);

                    for (d = 0; d < max; d++) {
                        r1 = r1 + c[d].getRed();
                    }
                    r1 = r1 / (max);

                    for (d = 0; d < max; d++) {
                        g1 = g1 + c[d].getGreen();
                    }
                    g1 = g1 / (max);

                    for (d = 0; d < max; d++) {
                        b1 = b1 + c[d].getBlue();
                    }
                    b1 = b1 / (max);
                    int sum1 = (a1 << 24) + (r1 << 16) + (g1 << 8) + b1;
                    bi.setRGB(y, x, (int) (sum1));

                }
            }
            File blurfile1 = new File("blur.jpg");
            ImageIO.write(bi, "jpg", blurfile1);


            Graphics2D g = img.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            BufferedImage overlayedImage = overlayImages(img, bi);
            File  output= new File("overlay.jpg");
            ImageIO.write(overlayedImage, "jpg", output);
            System.out.print("Output file location: C:\\Users\\haritha\\IdeaProjects\\restconnection\\overlay.jpg\n");


        } catch (IOException e) {
            System.out.println("Image could not be read");
        }
    }
    private static BufferedImage resizeImage(final Image image, int width, int height) {
        int targetw = 30;
        int targeth = 20;


        if (width<50) {
            targetw=50;
            targeth=(targetw/width)*height;

        }
        else if (height<50){
            targeth=50;
            targetw=(targeth/height)*width;
        }

        Image tmp = image.getScaledInstance(targetw, targeth, Image.SCALE_SMOOTH );
        BufferedImage resized = new BufferedImage(targetw/2, targeth, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
    public static BufferedImage overlayImages(BufferedImage bgImage,
                                              BufferedImage fgImage) {



        if (fgImage.getHeight() > bgImage.getHeight()
                || fgImage.getWidth() > fgImage.getWidth()) {
            JOptionPane.showMessageDialog(null,
                    "Foreground Image Is Bigger In One or Both Dimensions"
                            + "nCannot proceed with overlay."
                            + "nn Please use smaller Image for foreground");
            return null;
        }


        Graphics2D g = bgImage.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(bgImage, 0, 0, null);



        g.drawImage(fgImage, 73, 65, null);

        g.dispose();
        return bgImage;
    }

}
