package cn.edu.pku.zhangyh.miniweather;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.zhangyh.bean.TodayWeather;
import cn.edu.pku.zhangyh.bean.WeekWeather;
import cn.edu.pku.zhangyh.util.NetUtil;

/**
 * Created by zhangyh on 2016/9/26.
 */
public class MainActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    //六日天气
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private List<View> views;
    private ImageView[] dots;
    private int[] ids = {R.id.iv1, R.id.iv2};
    private int[] dateId = {R.id.date1, R.id.date2, R.id.date3, R.id.date4, R.id.date5, R.id.date6};
    private int[] temperatureId = {R.id.temperature1, R.id.temperature2, R.id.temperature3, R.id.temperature4, R.id.temperature5, R.id.temperature6};
    private int[] climateId = {R.id.climate1, R.id.climate2, R.id.climate3, R.id.climate4, R.id.climate5, R.id.climate6};
    private int[] windId = {R.id.wind1, R.id.wind2, R.id.wind3, R.id.wind4, R.id.wind5, R.id.wind6};
    private int[] weatherImgId = {R.id.image1, R.id.image2, R.id.image3, R.id.image4, R.id.image5, R.id.image6};
    private TextView[] dateWeek,temperatureWeek, climateWeek, windWeek;
    private ImageView[] weatherImgWeek;

    private static final int UPDATE_TODAY_WEATHER=1;
    private static final int UPDATE_WEEK_WEATHER=2;
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                case UPDATE_WEEK_WEATHER:
                    updateWeekWeather((List<WeekWeather>) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络ok");
            Toast.makeText(MainActivity.this, "网络ok", Toast.LENGTH_LONG).show();
        } else {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了", Toast.LENGTH_LONG).show();
        }
        mCitySelect=(ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        initView();


        //六日天气
        initViews();
        initdots();
    }

//   初始化项目，调用最后一次的数据信息
    void initView() {
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);

/*      利用SharedPreferences读取历史数据
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String cityCode = sharedPreferences.getString("main_city-code", "101010100");

        city_name_Tv.setText(sharedPreferences.getString("cityTv", "N/A"));
        cityTv.setText(sharedPreferences.getString("cityTv", "N/A"));
        timeTv.setText(sharedPreferences.getString("timeTv", "N/A"));
        humidityTv.setText(sharedPreferences.getString("humidityTv", "N/A"));
        pmDataTv.setText(sharedPreferences.getString("pmDataTv", "N/A"));
        climateTv.setText(sharedPreferences.getString("climateTv", "N/A"));
        windTv.setText(sharedPreferences.getString("windTv", "N/A"));
        pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
        weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
        */
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");


    }

   //存储数据
   /* private void postData(String cityCode){
        SharedPreferences.Editor editor=getSharedPreferences("config",MODE_PRIVATE).edit();
        editor.putString("main_city-code", cityCode);
        editor.putString("city_name_Tv",city_name_Tv.getText().toString());
        editor.putString("cityTv",cityTv.getText().toString());
        editor.putString("timeTv",timeTv.getText().toString());
        editor.putString("humidityTv",humidityTv.getText().toString());
        editor.putString("pmDataTv",pmDataTv.getText().toString());
        editor.putString("climateTv",climateTv.getText().toString());
        editor.putString("windTv",windTv.getText().toString());
        editor.commit();
    }*/
    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.title_city_manager){
            Intent i=new Intent(this,SelectCity.class);
            //startActivity(i);
            startActivityForResult(i,1);
        }
        if (view.getId() == R.id.title_update_btn) {
            mUpdateBtn.setVisibility(View.GONE);
            ProgressBar mUpateProgress =(ProgressBar) findViewById(R.id.title_update_progress);
            mUpateProgress.setVisibility(View.VISIBLE);

       /*     ImageView mShare = (ImageView)findViewById(R.id.title_share);
            RelativeLayout.LayoutParams params =new RelativeLayout.LayoutParams(45,45);
            params.addRule(RelativeLayout.LEFT_OF,mUpateProgress.getId());
            mShare.setLayoutParams(params);*/

            //动态调整控件位置
            ImageView mShare = (ImageView)findViewById(R.id.title_share);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mShare.getLayoutParams();
            params.addRule(RelativeLayout.LEFT_OF,mUpateProgress.getId());
            mShare.setLayoutParams(params);

            SharedPreferences sharedPreferences = getSharedPreferences("config2", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city-code", "101010100");
            Log.d("myWeather", cityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(cityCode);
            } else {
                Log.d("myWeather", "网络OK");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==1 && resultCode==RESULT_OK){
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather","选择的城市代码为"+newCityCode);

            if(NetUtil.getNetworkState(this)!=NetUtil.NETWORN_NONE){
                Log.d("myWeather","网络ok");
                queryWeatherCode(newCityCode);
            }else{
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void queryWeatherCode(String cityCode) {
      //  postData(cityCode);
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather=null;
                List<WeekWeather> weekWeathers=null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);
                    todayWeather = parseXML(responseStr);
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }
                    //六日天气
                    weekWeathers = parseXML1(responseStr);
                    if (weekWeathers != null) {
                        Log.d("myWeather", weekWeathers.get(0).toString());
                        Log.d("myWeather", weekWeathers.get(1).toString());
                        Log.d("myWeather", weekWeathers.get(2).toString());
                        Log.d("myWeather", weekWeathers.get(3).toString());
                        Log.d("myWeather", weekWeathers.get(4).toString());
                        Message msg = new Message();
                        msg.what = UPDATE_WEEK_WEATHER;
                        msg.obj = weekWeathers;
                        mHandler.sendMessage(msg);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather=null;
        int fengxiangCount=0;
        int fengliCount=0;
        int dateCount=0;
        int highCount=0;
        int lowCount=0;
        int typeCount=0;
        try{
            XmlPullParserFactory fac =XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType =xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            while(eventType!=XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather!=null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang( xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli( xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType=xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return todayWeather;
    }

    void updateTodayWeather(TodayWeather todayWeather) {
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+"发布");
        humidityTv.setText("湿度:"+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());

        if(todayWeather.getPm25()==null){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);  //当没有pm2.5数据，没有该语句会导致闪退，待修改
        }else if(Integer.parseInt(todayWeather.getPm25())<=50){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
        }else if(Integer.parseInt(todayWeather.getPm25())<=100){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
        }else if(Integer.parseInt(todayWeather.getPm25())<=150){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
        }else if(Integer.parseInt(todayWeather.getPm25())<=200){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
        } else if (Integer.parseInt(todayWeather.getPm25()) <= 300) {
            pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
        } else {
            pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
        }

        if(todayWeather.getType()==null){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
        }else if(todayWeather.getType().equals("暴雪")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
        }else if(todayWeather.getType().equals("暴雨")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
        }else if(todayWeather.getType().equals("大暴雨")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
        }else if(todayWeather.getType().equals("大雪")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
        }else if(todayWeather.getType().equals("大雨")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
        }else if(todayWeather.getType().equals("多云")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
        }else if(todayWeather.getType().equals("雷阵雨")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
        }else if(todayWeather.getType().equals("雷阵雨冰雹")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
        }else if(todayWeather.getType().equals("晴")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
        }else if(todayWeather.getType().equals("沙尘暴")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
        }else if(todayWeather.getType().equals("特大暴雨")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
        }else if(todayWeather.getType().equals("雾")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
        }else if(todayWeather.getType().equals("小雪")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
        }else if(todayWeather.getType().equals("小雨")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
        }else if(todayWeather.getType().equals("阴")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
        }else if(todayWeather.getType().equals("雨夹雪")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
        }else if(todayWeather.getType().equals("阵雪")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
        }else if(todayWeather.getType().equals("阵雨")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
        }else if(todayWeather.getType().equals("中雪")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
        }else if(todayWeather.getType().equals("中雨")){
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
        }
        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();

        ProgressBar mUpateProgress =(ProgressBar) findViewById(R.id.title_update_progress);
        mUpateProgress.setVisibility(View.GONE);
        mUpdateBtn.setVisibility(View.VISIBLE);

        //动态调整控件位置
        ImageView mShare = (ImageView)findViewById(R.id.title_share);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mShare.getLayoutParams();
        params.addRule(RelativeLayout.LEFT_OF, mUpdateBtn.getId());
        mShare.setLayoutParams(params);
    }


    //六日天气
    private void initdots(){
        dots = new ImageView[views.size()];
        for(int i=0;i<views.size();i++){
            dots[i] = (ImageView) findViewById(ids[i]);
        }
    }
    private void initViews(){
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.week1, null));
        views.add(inflater.inflate(R.layout.week2, null));
        viewPagerAdapter = new ViewPagerAdapter(views,this);
        viewPager = (ViewPager)findViewById(R.id.viewpager1);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(this);

        //六日天气
        dateWeek = new TextView[6];
        temperatureWeek = new TextView[6];
        climateWeek = new TextView[6];
        windWeek = new TextView[6];
        weatherImgWeek = new ImageView[6];

        for(int i =0;i<6;i++){
            dateWeek[i] = (TextView) views.get(i/3).findViewById(dateId[i]);
            temperatureWeek[i] = (TextView) views.get(i/3).findViewById(temperatureId[i]);
            climateWeek[i] = (TextView) views.get(i/3).findViewById(climateId[i]);
            windWeek[i] = (TextView) views.get(i/3).findViewById(windId[i]);
            weatherImgWeek[i] = (ImageView) views.get(i/3).findViewById(weatherImgId[i]);
        }

    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    @Override
    public void onPageSelected(int position) {
        for(int a=0;a<ids.length;a++){
            if(a==position){
                dots[a].setImageResource(R.drawable.page_indicator_focused);
            }else{
                dots[a].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }
    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private List<WeekWeather> parseXML1(String xmldata){
        List<WeekWeather> weekWeathers = new ArrayList<WeekWeather>();
        WeekWeather weekWeather = null;
        try{
            XmlPullParserFactory fac =XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType =xmlPullParser.getEventType();
            while(eventType!=XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("weather")){
                            weekWeather = new WeekWeather();
                            weekWeathers.add(weekWeather);
                            Log.d("myWeather","ddd");
                        }
                        if(weekWeather!=null) {
                            if (xmlPullParser.getName().equals("fengli")) {
                                eventType = xmlPullParser.next();
                                weekWeather.setFengli( xmlPullParser.getText());
                                Log.d("myWeather","ffff");
                            } else if (xmlPullParser.getName().equals("date")) {
                                eventType = xmlPullParser.next();
                                weekWeather.setDate(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("high")) {
                                eventType = xmlPullParser.next();
                                weekWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                            } else if (xmlPullParser.getName().equals("low")) {
                                eventType = xmlPullParser.next();
                                weekWeather.setLow(xmlPullParser.getText().substring(2).trim());
                            } else if (xmlPullParser.getName().equals("type")) {
                                eventType = xmlPullParser.next();
                                weekWeather.setType(xmlPullParser.getText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType=xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return weekWeathers;
    }

    void updateWeekWeather(List<WeekWeather> weekWeather) {
        for(int i=0;i<weekWeather.size();i++) {
            dateWeek[i].setText(weekWeather.get(i).getDate());
            temperatureWeek[i].setText(weekWeather.get(i).getHigh() + "~" + weekWeather.get(i).getLow());
            climateWeek[i].setText(weekWeather.get(i).getType());
            windWeek[i].setText(weekWeather.get(i).getFengli());

            if (weekWeather.get(i).getType() == null) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_baoxue);
            } else if (weekWeather.get(i).getType().equals("暴雪")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_baoxue);
            } else if (weekWeather.get(i).getType().equals("暴雨")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_baoyu);
            } else if (weekWeather.get(i).getType().equals("大暴雨")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
            } else if (weekWeather.get(i).getType().equals("大雪")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_daxue);
            } else if (weekWeather.get(i).getType().equals("大雨")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_dayu);
            } else if (weekWeather.get(i).getType().equals("多云")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_duoyun);
            } else if (weekWeather.get(i).getType().equals("雷阵雨")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
            } else if (weekWeather.get(i).getType().equals("雷阵雨冰雹")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
            } else if (weekWeather.get(i).getType().equals("晴")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_qing);
            } else if (weekWeather.get(i).getType().equals("沙尘暴")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_shachenbao);
            } else if (weekWeather.get(i).getType().equals("特大暴雨")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
            } else if (weekWeather.get(i).getType().equals("雾")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_wu);
            } else if (weekWeather.get(i).getType().equals("小雪")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
            } else if (weekWeather.get(i).getType().equals("小雨")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
            } else if (weekWeather.get(i).getType().equals("阴")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_yin);
            } else if (weekWeather.get(i).getType().equals("雨夹雪")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
            } else if (weekWeather.get(i).getType().equals("阵雪")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_zhenxue);
            } else if (weekWeather.get(i).getType().equals("阵雨")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_zhenyu);
            } else if (weekWeather.get(i).getType().equals("中雪")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_zhongxue);
            } else if (weekWeather.get(i).getType().equals("中雨")) {
                weatherImgWeek[i].setImageResource(R.drawable.biz_plugin_weather_zhongyu);
            }
        }
    }

}
