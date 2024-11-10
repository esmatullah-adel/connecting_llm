package adel.esmatullah.connectingllmjava;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.openai.com/v1/images/generations";
    private static final String API_KEY = "type your api here";
    private static final int CAMERA_REQUEST_CODE = 1;

    private Uri photoUri;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCapturePhoto = findViewById(R.id.btnCapturePhoto);
        TextView tvResponse = findViewById(R.id.tvResponse);
        ImageView ivCapturedImage = findViewById(R.id.ivCapturedImage);
        EditText etGoal = findViewById(R.id.editTextGoal);
        Button btnGeneratePhoto = findViewById(R.id.btnGeneratePhoto);
        btnGeneratePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start SecondActivity
                Intent intent = new Intent(MainActivity.this, GenerateImageActivity.class);
                startActivity(intent);
            }
        });

        // Handling result from the camera intent
        ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (photoFile.exists()) {
                            ivCapturedImage.setImageURI(photoUri); // Display the captured image
                            try {
                                sendImageRequest("Describe the image.", photoFile, tvResponse);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Log.e("Camera Error", "Failed to capture image");
                    }
                });

        btnCapturePhoto.setOnClickListener(v -> {
            try {
                if (etGoal.getText().toString().trim().isEmpty()) {
                    tvResponse.setText("Please enter a goal.");
                    return;
                }
                photoFile = createImageFile();
                photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("Pictures");
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void sendImageRequest(String prompt, File imageFile, TextView tvResponse) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", "gpt-4-vision-preview")
                .addFormDataPart("prompt", prompt)
                .addFormDataPart("file", "image.jpg",
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() ->
                            tvResponse.setText(response.body().toString()));
                } else {
                    runOnUiThread(() ->
                            tvResponse.setText(response.toString()));
                    Log.e("OpenAI Error", response.toString());
                }
            }
        });
    }
}