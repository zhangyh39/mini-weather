package cn.edu.pku.zhangyh.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.zhangyh.app.MyApplication;
import cn.edu.pku.zhangyh.bean.City;

/**
 * Created by zhangyh on 2016/10/18.
 */
public class SelectCity extends Activity implements View.OnClickListener {
    private ImageView mBackBtn;

    // ListView
    private ListView mlistView;
    private MyApplication cityApplication;
    private List<City> mcity;
    private ArrayList<String> cityName = new ArrayList<String>();
    private ArrayList<String> newlist = new ArrayList<String>();
    private ArrayList<String> cityName1 = new ArrayList<String>();
    private ArrayList<String> cityId= new ArrayList<String>();
    private String selectId;
    private String selectCity;
    private TextView mtitileName;

    private EditText mEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        mlistView = (ListView) findViewById(R.id.list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectCity.this, android.R.layout.simple_list_item_1, cityName);
        mlistView.setAdapter(adapter);

        cityApplication = (MyApplication) getApplication();
        mcity = cityApplication.getCityList();
        for (int i = 0; i < mcity.size(); i++) {
            cityName.add(mcity.get(i).getProvince()+"-"+mcity.get(i).getCity());
            cityId.add(mcity.get(i).getNumber());
            cityName1 = cityName;
        }

        mtitileName = (TextView) findViewById(R.id.title_name);

        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectCity = cityName1.get(i);
                Toast.makeText(SelectCity.this, "你选择了："+selectCity, Toast.LENGTH_SHORT).show();
                mtitileName.setText("当前城市："+selectCity);
                int j = cityName.indexOf(selectCity);
                selectId = cityId.get(j);
            }
        });


        mEditText = (EditText)findViewById(R.id.city_search);



        TextWatcher mTextWatcher = new TextWatcher() {
            private CharSequence temp;
            private int editStart;
            private int editEnd;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                temp = s;
                Log.d("myWeather","beforeTextChanged:"+temp);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newlist.clear();
                if(mEditText.getText()!=null){
                    String st = mEditText.getText().toString();
                    newlist = getNewData(st);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectCity.this, android.R.layout.simple_list_item_1, newlist);
                    mlistView.setAdapter(adapter);
                    cityName1 = newlist;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            private ArrayList<String> getNewData(String s) {
                //遍历list
                for (int i = 0; i < cityName.size(); i++) {
                    //如果遍历到的名字包含所输入字符串
                    if (cityName.get(i).contains(s)) {
                        //将遍历到的元素重新组成一个list
                        newlist.add(cityName.get(i));
                    }
                }
                return newlist;
            }
        };

        mEditText.addTextChangedListener(mTextWatcher);
    }

    /*
    public void initData(){
        List<City> list = new ArrayList<City>();
        list = cityDb.getAllCity();
        String[] cityData = new String[10];
        for(int i=0;i<10;i++){
            cityData[i] = list.get(i).getCity();
            Log.d("ListView","1aw");
        }
        data = cityData;
    }
    */

    public void onClick(View v){
        switch(v.getId()){
            case R.id.title_back:
                if(selectId!=null){
                    SharedPreferences.Editor editor=getSharedPreferences("config",MODE_PRIVATE).edit();
                    editor.putString("main_city-code",selectId);
                    editor.commit();
                }else{
                    SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
                    selectId = sharedPreferences.getString("main_city-code","101010100");
                }
                Intent i=new Intent();
                i.putExtra("cityCode",selectId);
                setResult(RESULT_OK,i);
                finish();
                break;
            default:
                break;
        }
    }


}
