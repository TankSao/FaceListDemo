package com.example.administrator.facelistdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.support.v4.view.ViewPager.OnPageChangeListener;

public class MainActivity extends AppCompatActivity {

    private ImageView iv;
    private LinearLayout chat_face_container;
    private ViewPager mViewPager;
    private int columns = 5;
    private int rows = 4;
    private List<View> views = new ArrayList<View>();
    private List<String> staticFacesList;
    private LinearLayout mDotsLayout;
    private EditText input;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = (ImageView) findViewById(R.id.image_face);
        chat_face_container = findViewById(R.id.chat_face_container);
        mDotsLayout = findViewById(R.id.face_dots_container);
        mViewPager = findViewById(R.id.face_viewpager);
        input = findViewById(R.id.input_sms);
        mViewPager.setOnPageChangeListener(new PageChangeListener());
        initStaticFaces();
        initViewPager();
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftInputView();//隐藏软键盘
                if (chat_face_container.getVisibility() == View.GONE) {
                    chat_face_container.setVisibility(View.VISIBLE);
                } else {
                    chat_face_container.setVisibility(View.GONE);
                }
            }
        });
    }

    //页面变化改变dots
    class PageChangeListener implements OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
                mDotsLayout.getChildAt(i).setSelected(false);
            }
            mDotsLayout.getChildAt(arg0).setSelected(true);
        }

    }

    //获取页数
    private int getPagerCount() {
        int count = staticFacesList.size();
        return count % (columns * rows - 1) == 0 ? count / (columns * rows - 1)
                : count / (columns * rows - 1) + 1;
    }
    //初始化ViewPager
    private void initViewPager() {
        for (int i = 0; i < getPagerCount(); i++) {
            views.add(viewPagerItem(i));
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(16, 16);
            mDotsLayout.addView(dotsItem(i), params);
        }
        FaceVPAdapter mVpAdapter = new FaceVPAdapter(views);
        mViewPager.setAdapter(mVpAdapter);
        mDotsLayout.getChildAt(0).setSelected(true);
    }

    private ImageView dotsItem(int position) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dot_image, null);
        ImageView iv = layout.findViewById(R.id.face_dot);
        iv.setId(position);
        return iv;
    }

    private View viewPagerItem(int position) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.face_gridview, null);//表情布局
        GridView gridview = layout.findViewById(R.id.chart_face_gv);
        /**
         * 注：因为每一页末尾都有一个删除图标，所以每一页的实际表情columns *　rows　－　1; 空出最后一个位置给删除图标
         * */
        List<String> subList = new ArrayList<String>();
        subList.addAll(staticFacesList
                .subList(position * (columns * rows - 1),
                        (columns * rows - 1) * (position + 1) > staticFacesList
                                .size() ? staticFacesList.size() : (columns
                                * rows - 1)
                                * (position + 1)));
        /**
         * 末尾添加删除图标
         * */
        subList.add("emotion_del_normal.png");
        FaceGVAdapter mGvAdapter = new FaceGVAdapter(subList, this);
        gridview.setAdapter(mGvAdapter);
        gridview.setNumColumns(columns);
        // 单击表情执行的操作
        gridview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String png = ((TextView) ((LinearLayout) view).getChildAt(1)).getText().toString();
                    if (!png.contains("emotion_del_normal")) {// 如果不是删除图标
                        insert(getFace(png));
                    } else {
                        delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String png = ((TextView) ((LinearLayout) view).getChildAt(1)).getText().toString();
                if (!png.contains("emotion_del_normal")) {// 如果不是删除图标
                } else {
                    deleteAll();
                }
                return false;
            }
        });

        return gridview;
    }
    private void insert(CharSequence text) {
        int iCursorStart = Selection.getSelectionStart((input.getText()));
        int iCursorEnd = Selection.getSelectionEnd((input.getText()));
        if (iCursorStart != iCursorEnd) {
            input.getText().replace(iCursorStart, iCursorEnd, "");
        }
        int iCursor = Selection.getSelectionEnd((input.getText()));
        input.getText().insert(iCursor, text);
    }
    private void delete() {
        if (input.getText().length() != 0) {
            int iCursorEnd = Selection.getSelectionEnd(input.getText());
            int iCursorStart = Selection.getSelectionStart(input.getText());
            if (iCursorEnd > 0) {
                if (iCursorEnd == iCursorStart) {
                    if (isDeletePng(iCursorEnd)) {
                        String st = "#[face/png/f_static_000.png]#";
                        input.getText().delete(
                                iCursorEnd - st.length(), iCursorEnd);
                    } else {
                        input.getText().delete(iCursorEnd - 1,
                                iCursorEnd);
                    }
                } else {
                    input.getText().delete(iCursorStart,
                            iCursorEnd);
                }
            }
        }
    }
    private void deleteAll() {
        input.setText("");
    }

    private boolean isDeletePng(int cursor) {
        String st = "#[face/png/f_static_000.png]#";
        String content = input.getText().toString().substring(0, cursor);
        if (content.length() >= st.length()) {
            String checkStr = content.substring(content.length() - st.length(),
                    content.length());
            String regex = "(\\#\\[face/png/f_static_)\\d{3}(.png\\]\\#)";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(checkStr);
            return m.matches();
        }
        return false;
    }

    private SpannableStringBuilder getFace(String png) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        try {
            String tempText = "#[" + png + "]#";
            sb.append(tempText);
            Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open(png));
            //bitmap = gethalfBitmap(bitmap);
            ImageSpan is = new ImageSpan(MainActivity.this, bitmap);
            sb.setSpan(is, sb.length()
                            - tempText.length(), sb.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb;
    }
    //Bitmap按比例缩放
    Bitmap gethalfBitmap(Bitmap bmp) {
        int destWidth = bmp.getWidth() / 2;
        int destHeight = bmp.getHeight() / 2;
        Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, destWidth, destHeight, true);
        return bmp2;
    }

    //初始化表情列表
    private void initStaticFaces() {
        try {
            staticFacesList = new ArrayList<String>();
            String[] faces = getAssets().list("face/png");
            for (int i = 0; i < faces.length; i++) {
                staticFacesList.add(faces[i]);
            }
            //去掉删除图片
            staticFacesList.remove("emotion_del_normal.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //隐藏键盘
    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
