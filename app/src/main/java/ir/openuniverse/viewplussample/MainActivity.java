package ir.openuniverse.viewplussample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ir.openuniverse.viewplus.ViewPlus;

public class MainActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		new ViewPlus() {};
	}
}
