package com.example.bobby.map;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private AMap aMap;
    private ListView listView;
    protected List<String> mlist = new ArrayList<>();
    private SearchView searchView;
    private LatLng AimlatLng;
    protected Button button;
    private String City = null;
    private LatLng MyPosition;
    private Spinner spinner;
    private Poi start;
    private Poi end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        initMap(savedInstanceState);
        listView = (ListView) findViewById(R.id.Tips_listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = mlist.get(position);
                searchView.setQuery(name, true);
            }
        });
        button = (Button) findViewById(R.id.guide);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmapNaviPage.getInstance().showRouteActivity(MainActivity.this, new AmapNaviParams(start, null, end, AmapNaviType.DRIVER), new MyNaviInfoCallback());
                button.setVisibility(View.GONE);
            }
        });
    }

    private void initMap(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.setMyLocationEnabled(true);
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                MyPosition = new LatLng(location.getLatitude(), location.getLongitude());
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MyPosition, 16));
            }
        });
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//开启定位按钮
    }

    private void searchGEO(final String position) {
        GeocodeSearch geocodeSearch = new GeocodeSearch(this);
        GeocodeSearch.OnGeocodeSearchListener listener = new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                listView.setVisibility(View.GONE);
                if (geocodeResult == null) {
                    Toast.makeText(MainActivity.this, "没有查找到有关地点", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    GeocodeAddress address = geocodeResult.getGeocodeAddressList().get(0);
                    AimlatLng = new LatLng(address.getLatLonPoint().getLatitude(),
                            address.getLatLonPoint().getLongitude());
                    final Marker marker = aMap.addMarker(new MarkerOptions().
                            position(AimlatLng)
                            .title(position)
                            .snippet("DefaultMarker")
                            .infoWindowEnable(true)
                            .visible(true));
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(AimlatLng, 13));
                    start = new Poi("", MyPosition, "");
                    end = new Poi("", AimlatLng, "");
                    button.setVisibility(View.VISIBLE);
                }
            }
        };
        GeocodeQuery query;
        if (City == null) {
            query = new GeocodeQuery(position, "珠海");
        } else
            query = new GeocodeQuery(position, City);
        geocodeSearch.setOnGeocodeSearchListener(listener);
        geocodeSearch.getFromLocationNameAsyn(query);
    }

    private void getInputTips(final String newText) {
        Inputtips.InputtipsListener listener = new Inputtips.InputtipsListener() {
            @Override
            public void onGetInputtips(final List<Tip> list, int i) {
                mlist.clear();
                for (Tip tip : list) {
                    mlist.add(tip.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_list_item_1, mlist);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        };
        InputtipsQuery inputquery;
        if (City == null)
            inputquery = new InputtipsQuery(newText, "珠海");
        else
            inputquery = new InputtipsQuery(newText, City);
        inputquery.setCityLimit(true);//限制在当前城市
        Inputtips inputTips = new Inputtips(MainActivity.this, inputquery);
        inputTips.setInputtipsListener(listener);
        inputTips.requestInputtipsAsyn();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem searchViewItem = menu.findItem(R.id.search_View);
        final MenuItem Spinner = menu.findItem(R.id.menu_spinner);
        spinner = (Spinner) Spinner.getActionView();
        final ArrayAdapter<CharSequence> CityAdapter = ArrayAdapter.createFromResource
                (MainActivity.this, R.array.city, R.layout.spinner_item);
        CityAdapter.setDropDownViewResource(R.layout.dropdown_stytle);
        spinner.setAdapter(CityAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                City = CityAdapter.getItem(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        searchView = (SearchView) searchViewItem.getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("查找地点，公交站，地铁");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchGEO(query);
                aMap.clear();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    getInputTips(newText);
                    listView.setVisibility(View.VISIBLE);
                } else {
                    button.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click(); //调用双击退出函数
        }
        return false;
    }

    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
            System.exit(0);
        }
    }
}
