package popo.dynamicform;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout linearLayout;
    Button button;
    Button form1,form2,form3,form4,btnFile;
    private ArrayList<Button> form = new ArrayList<>();
    int b = 0;
    Uri path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout = findViewById(R.id.ly_form);
        button = findViewById(R.id.btnGetData);
        btnFile = findViewById(R.id.btn_pilih_file);


        button.setOnClickListener(this);
        btnFile.setOnClickListener(this);


    }


    @Override
    public void onClick(View view) {


        form1 = findViewById(Integer.parseInt("1"));
        form2 = findViewById(Integer.parseInt("2"));
        form3 = findViewById(Integer.parseInt("3"));
        form4 = findViewById(Integer.parseInt("4"));
        if(form1 != null) {
            form1.setOnClickListener(this);
        }

        if(view.getId()== R.id.btnGetData){
            b++;
            Button button = new Button(this);
            button.setText("Pilih File " + b);
            button.setId(ViewCompat.generateViewId());
            linearLayout.setWeightSum(1f);
            linearLayout.addView(button);

            Toast.makeText(MainActivity.this,String.valueOf(button.getId()),Toast.LENGTH_LONG).show();
            form.add(button);
            System.out.println("dynamic2 " + form);

        }

        if (view.getId() == R.id.btn_pilih_file) {
            System.out.println("dynamic"+form1.getId());
            Toast.makeText(MainActivity.this, "penuh", Toast.LENGTH_LONG).show();
        }
        for (int i =0;i<form.size();i++){
            if(form!=null && !form.isEmpty()) {
                System.out.println("dynamic " + form.get(i));
                Button btn = form.get(i);
                btn = findViewById(b);
                System.out.println(btn.getId());
                btn.setOnClickListener(this);
                if (view.getId() == form.get(i).getId()) {
//                    Toast.makeText(MainActivity.this, "penuh "+i, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.setType("application/pdf");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select PDF"), 1);
                }
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if(requestCode == 1){
                path = data.getData();
                Toast.makeText(MainActivity.this,"DYNAMIC ALAMAT "+ path,Toast.LENGTH_LONG).show();
            }
        }
    }
}
