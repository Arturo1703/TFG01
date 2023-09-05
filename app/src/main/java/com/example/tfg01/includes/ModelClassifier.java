package com.example.tfg01.includes;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.tfg01.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ModelClassifier {
    private final Interpreter tfliteInterpreter;
    private final TensorProcessor probabilityProcessor;
    private final List<String> labels = Arrays.asList("normal", "porn");

    public ModelClassifier(Context context) throws IOException {
        tfliteInterpreter = new Interpreter(loadModelFile(context));
        probabilityProcessor = new TensorProcessor.Builder()
                .add(new NormalizeOp(0.0f, 1.0f))
                .build();
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        FileInputStream inputStream = new FileInputStream(context.getAssets().openFd("model.tflite").getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = context.getAssets().openFd("model.tflite").getStartOffset();
        long declaredLength = context.getAssets().openFd("model.tflite").getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public ClassificationResult classify(Bitmap bitmap) {
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(180, 180, ResizeOp.ResizeMethod.BILINEAR))
                .build();
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(bitmap);
        tensorImage = imageProcessor.process(tensorImage);

        // Corre la inferencia
        TensorBuffer outputFeature0 = TensorBuffer.createFixedSize(new int[]{1, labels.size()}, DataType.FLOAT32);
        tfliteInterpreter.run(tensorImage.getBuffer(), outputFeature0.getBuffer());

        // Procesa el resultado
        float[] outputData = outputFeature0.getFloatArray();

        // Aplica softmax para obtener las probabilidades
        float[] probabilities = applySoftmax(outputData);
        // Obtiene la clasificación con la mayor probabilidad y el resultado
        int maxIndex = 0;
        float maxConfidence = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxConfidence) {
                maxConfidence = probabilities[i];
                maxIndex = i;
            }
        }
        String classifiedLabel = labels.get(maxIndex);
        return new ClassificationResult(classifiedLabel, maxConfidence);
    }

    private float[] applySoftmax(float[] logits) {
        float[] softmaxValues = new float[logits.length];
        float maxLogit = logits[0];
        for (float logit : logits) {
            if (logit > maxLogit) {
                maxLogit = logit;
            }
        }
        float sum = 0.0f;
        for (int i = 0; i < logits.length; i++) {
            softmaxValues[i] = (float) Math.exp(logits[i] - maxLogit);
            sum += softmaxValues[i];
        }
        for (int i = 0; i < logits.length; i++) {
            softmaxValues[i] = softmaxValues[i] / sum;
        }
        return softmaxValues;
    }

    public static ByteBuffer convertirBitmapAByteBuffer(Bitmap bitmap) {
        // Asegurarse de que la imagen tiene las dimensiones correctas
        if (bitmap.getWidth() != 180 || bitmap.getHeight() != 180) {
            bitmap = Bitmap.createScaledBitmap(bitmap, 180, 180, true);
        }

        // Crear un ByteBuffer para almacenar los datos de la imagen
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 180 * 180 * 3); // Float32 para cada píxel RGB
        byteBuffer.order(ByteOrder.nativeOrder());

        // Normalizar los valores de los píxeles y escribirlos en el ByteBuffer
        for (int y = 0; y < 180; y++) {
            for (int x = 0; x < 180; x++) {
                int pixelValue = bitmap.getPixel(x, y);
                float r = ((pixelValue >> 16) & 0xFF) / 255.0f;
                float g = ((pixelValue >> 8) & 0xFF) / 255.0f;
                float b = (pixelValue & 0xFF) / 255.0f;
                byteBuffer.putFloat(r);
                byteBuffer.putFloat(g);
                byteBuffer.putFloat(b);
            }
        }

        return byteBuffer;
    }


    public void close() {
        tfliteInterpreter.close();
    }

    public static class ClassificationResult {
        public final String label;
        public final float confidence;

        public ClassificationResult(String label, float confidence) {
            this.label = label;
            this.confidence = confidence;
        }
    }
}