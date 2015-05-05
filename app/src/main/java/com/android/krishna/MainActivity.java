package com.android.krishna;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.payUMoney.sdk.Session;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity {
    HashMap<String,String> params;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn =  (Button)findViewById(R.id.btn);
        params = new HashMap<String, String>();


        String hashSequence = "asljdhaifffgfgyffsyuhfgsuyfsb";
        String hash = hashCal("SHA-512", hashSequence);


        params.put("TxnId","101");
        params.put("hash",hash);
        params.put("key","JBZaLc");
        params.put("Amount","55");
        params.put("MerchantId","JBZaLc");

        params.put("ProductInfo","juta");
        params.put("SURL","1");
        params.put("FURL","1");
        params.put("firstName","krishna");
        params.put("Email","k@gmail.com");
        params.put("Phone","89050785358");






        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Session.startPaymentProcess(MainActivity.this, params);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Session.PAYMENT_SUCCESS){
            if(resultCode == RESULT_OK){
                Toast.makeText(MainActivity.this,"Payment done",Toast.LENGTH_LONG).show();
            }

            if(resultCode == RESULT_CANCELED){
                Toast.makeText(MainActivity.this,"Payment Failed",Toast.LENGTH_LONG).show();
            }
        }



    }

    public static String hashCal(String type, String str)
    {
        byte[] hashseq = str.getBytes();
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest algorithm = MessageDigest.getInstance(type);
            algorithm.reset();
            algorithm.update(hashseq);
            byte messageDigest[] = algorithm.digest();
            for (int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xFF & messageDigest[i]);
                if (hex.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException nsae) {
        }
        return hexString.toString();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
