package com.example.tfg01.includes;

import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback;
import com.arthenica.ffmpegkit.ReturnCode;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;

/*
    Clase para extraer los keyframes de un video dado utilizando la librería FFmpeg kit adaptada a Android
    Libería: https://github.com/arthenica/ffmpeg-kit
    Versión: com.arthenica:ffmpeg-kit-full:5.1.LTS
    Utiliza a su vez la libería OpenCV para comprarar y descartar los frames extraidos. Suponemos que
    con una similitud inferior al 98% son iguales.
    Libería: https://opencv.org/releases/
    Version: OpenCV - 4.7.0-dev
 */

public class KeyFrames {

    /*
        Método que ejecuta el comando para extraer los keyFrames de la ruta absoluta de un video pasado por parametro
        y devuelve los keyframes (en .jpg) del video en la ruta deseada. También se pasa el nombre del archivo
        del video que se usará para crear la subcarpeta correspondiente con el nombre completo de dicho archivo
        @params String url_video, String destFolder, String fileName
        @return void
     */
    public static void executeComandoKeyFrames(String url_video, String destFolder, String fileName) throws Exception {
        try {
            String subFolder = fileName;
            String filePath = "";
            String fileExtn = ".jpg";

            String[] name = fileName.split("\\.");
            String nameKeyFrame = name[0];

            //Directorio
            File directory = new File(destFolder + subFolder);
            int dirNo = 0;
            while (directory.exists()) {
                dirNo++;
                directory = new File(destFolder + subFolder + " (" + dirNo + ")");
            }
            directory.mkdirs();

            File dest = new File(directory, nameKeyFrame + "%d" + fileExtn);
            filePath = dest.getAbsolutePath();
            String finalDirectoryPath = directory.getAbsolutePath();

            final String exe;
            exe = "-i \"" + url_video + "\" -v quiet -vf " +
                    "\"select=eq(pict_type\\,I)\"" +
                    " -vsync vfr -frame_pts true -qscale:v 2 \"" +
                    filePath + "\"";


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FFmpegSession executionId = FFmpegKit.executeAsync(exe,
                                new FFmpegSessionCompleteCallback() {
                                    @Override
                                    public void apply(FFmpegSession session) {
                                        if (ReturnCode.isSuccess(session.getReturnCode())) {
                                            Log.v("FFmpeg", "RESULT - output: \n" + fileName + "\n" + session.getOutput());

                                            //Llamada al comparador de frames de OpenCV
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    compararFramesOpenCV(finalDirectoryPath);
                                                }
                                            }).start();

                                        } else if (ReturnCode.isCancel(session.getReturnCode())) {
                                            Log.i("FFmpeg", "Async command execution cancelled by user.");
                                        } else {
                                            Log.i("FFmpeg", String.format("Async command execution failed with returnCode=%d.", session.getReturnCode().getValue()));
                                        }
                                    }
                                });
                    }catch (Exception e){
                        Log.e("FFmpeg", "Error en la llamada a la libería FFmpeg: "+e.getLocalizedMessage());
                        e.fillInStackTrace();
                    }
                }
            }).start();

        }catch (Exception e){
            Log.e("FFmpeg", "Error en FFmpeg comando: "+e.getLocalizedMessage());
            e.fillInStackTrace();
        }
    }

    /*
        Compara la similitud de los key frames extraidos mediante la librería OpenCV y
        borra aquellas imagenes con una similud mayor a un 40%. Se calcula la diferencia absoluta
        entre las imágenes utilizando Core.absdiff(). La función convertTo() se utiliza para
        asegurarse de que la matriz resultante sea del tipo CV_8UC1, es decir, una matriz de 8 bits
        sin signo con un canal. Finalmente, se calcula el porcentaje de diferencia basado en el
        número de píxeles que no son cero en la matriz resultante
        @params String carpeta
        @return void
    */

    private static void compararFramesOpenCV(String carpeta){
        try{
            File fcarpeta = new File(carpeta);
            File[] imagenes = fcarpeta.listFiles();

            for(File f : imagenes){
                if(f.isFile()) {
                    Mat img1 = Imgcodecs.imread(f.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                    for(File j : imagenes){
                        if(!j.getAbsolutePath().equals(f.getAbsolutePath())){
                            Mat img2 = Imgcodecs.imread(j.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);

                            //calcula la diferencia absoluta de las imagenes
                            Mat res = new Mat();
                            Core.absdiff(img1, img2, res);

                            //convierte el resultado a tipo entero
                            res.convertTo(res, CvType.CV_8UC1);

                            //porcentaje de diferencia basado en el numero de pixeles que no son cero
                            double percentage = (Core.countNonZero(res) * 100.0) / res.total();

                            Log.v("FRAMES", "Similarity: \n " +f.getAbsolutePath() + "\n" +
                                    j.getAbsolutePath() + "\n    " + percentage + "%");


                            // Si el porcentaje es menor que el corte podemos concluir que son iguales
                            // por tanto si es menor, se elimina la imagen
                            double corte = 98.0;
                            if(percentage < corte){
                                j.delete();
                                //actualizamos las imagenes que hay en la carpeta
                                imagenes = fcarpeta.listFiles();
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            Log.e("FRAMES", "Error al ejecutar OpenCV: "+e.getLocalizedMessage());
            e.fillInStackTrace();
        }
    }

}