package adel.esmatullah.connectingllmjava;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GenerateImageActivity extends AppCompatActivity {

    private ImageView imageView;
    private EditText editText;
    private TextView textView;
    private String stringURLEndPoint = "https://api.openai.com/v1/images/generations";
    private String stringAPIKey = "type your api here";
    private String stringOutput = "https://oaidalleapiprodscus.blob.core.windows.net/private/org-7vaCBBIvQdn5T6iXJ5d9rMCK/user-TuzNiGDDYMyNRWmuNsBV0Wc7/img-cimO4tIniv2MuvgI6GBIYOYB.png?st=2023-09-23T08%3A26%3A40Z&se=2023-09-23T10%3A26%3A40Z&sp=r&sv=2021-08-06&sr=b&rscd=inline&rsct=image/png&skoid=6aaadede-4fb3-4698-a8f6-684d7786b067&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2023-09-22T22%3A41%3A54Z&ske=2023-09-23T22%3A41%3A54Z&sks=b&skv=2021-08-06&sig=T%2Bt87bUWUEWBRmGMLoqNH4fIgOIvEwDN5KHUogVS%2BLQ%3D";
    private Bitmap bitmapOutputImage;

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_image);

        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);
    }

    public void buttonGenerateAIImage(View view){
        String stringInputText = editText.getText().toString();
        new Thread(() -> {
            try {
                String imageUrl = generateImageUrl(stringInputText);
                runOnUiThread(() -> {
                    if (imageUrl != null) {
                        //Picasso.get().load(imageUrl).into(imageView);
                        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Image generation failed", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> textView.setText(e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    public String generateImageUrl(String prompt) throws IOException, JSONException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        json.put("prompt", prompt);
        json.put("n", 1);
        json.put("size", "512x512");

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(stringURLEndPoint)
                .addHeader("Authorization", "Bearer " + stringAPIKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray data = jsonResponse.getJSONArray("data");
                if (data.length() > 0) {
                    return data.getJSONObject(0).getString("url");
                }
            } else {
                throw new IOException("Unexpected response: " + response);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void buttonShowImage(View view){
        textView.setText("Thread is in process");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(stringOutput);
                    bitmapOutputImage = BitmapFactory.decodeStream(url.openStream());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        while (thread.isAlive()){
            textView.setText("Thread is in process");
        }
        Bitmap bitmapFinalImage = Bitmap.createScaledBitmap(bitmapOutputImage,
                imageView.getWidth()
                ,imageView.getHeight(),
                true);
        imageView.setImageBitmap(bitmapFinalImage);
        textView.setText("Image Generation SUCCESSFUL!!!");
    }
}