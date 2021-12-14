package com.example.voca.Memorize.SpellCheck;

import android.util.Log;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.voca.R;
import com.example.voca.realtimeDB.VocaVO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SpellCheckViewHolderPage extends RecyclerView.ViewHolder {
    public static final String TAG = "SpellCheckViewHolderPage";

    private TextView vocaKor;
    private CheckBox memoCheck;
    private CheckBox starCheck;
    private EditText editAnswerText;
    private Button checkBtn;
    private TextView count;
    VocaVO vo;

    int mCount;
    String title;

    String today = getToday();

    // 실시간 사용자의 정보 받아옴
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();

    private DatabaseReference mDatabase_count;
    private DatabaseReference mDatabase_star;

    public SpellCheckViewHolderPage(View v, int mCount, String title){
        super(v);

        vocaKor=v.findViewById(R.id.voca_korean2);
        memoCheck=v.findViewById(R.id.memo_check_spell2);
        starCheck=v.findViewById(R.id.star_btn_spell2);
        editAnswerText=v.findViewById(R.id.text_answer_spell2);
        checkBtn=v.findViewById(R.id.btn_check_spell2);
        count=v.findViewById(R.id.count_spell2);

        this.mCount = mCount;
        this.title = title;
    }
    public void onBind(VocaVO vo,int totalNum,int dataSize){
        this.vo=vo;

        //암기버튼 이벤트 처리리
       memoCheck.setChecked(vo.getMemoCheck());// memoCheck 값의 여부에 따라 화면에 체크 유무 보여줌
       memoCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    vo.setMemoCheck(true);
                    // DB 내 memoCheck
                    mDatabase_count = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("userVoca").child(title)
                            .child(vo.getVocaEng())
                            .child("memoCheck");
                    mDatabase_count.setValue("true");
                    Log.d(TAG, "setMemocheck: " +mDatabase_count);

                    mCount++;
                    // DB 내 count(목표달성률)
                    mDatabase_count = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("goals")
                            .child(today)
                            .child("count");
                    mDatabase_count.setValue(mCount);
                    Log.d(TAG, "setMemocheck: " +mDatabase_count);
                }
                else {
                    vo.setMemoCheck(false);
                    // DB 내 memoCheck
                    mDatabase_count = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("userVoca").child(title)
                            .child(vo.getVocaEng())
                            .child("memoCheck");
                    mDatabase_count.setValue("false");

                    Log.d(TAG, "setMemocheck: " +mDatabase_count);

                    if(mCount > 0) {mCount--;}
                    // DB 내 count(목표달성률)
                    mDatabase_count = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("goals")
                            .child(today)
                            .child("count");
                    mDatabase_count.setValue(mCount);
                    Log.d(TAG, "setMemocheck: " +mDatabase_count);
                }
            }
        });
        //즐겨찾기 버튼 이벤트 처리
        starCheck.setChecked(vo.getStarCheck());// starCheck 값의 여부에 따라 화면에 체크 유무 보여줌
        starCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    vo.setStarCheck(true);
                    // DB 내 starCheck
                    mDatabase_star = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("userVoca").child(title)
                            .child(vo.getVocaEng())
                            .child("starCheck");
                    mDatabase_star.setValue("true");
                    Log.d(TAG, "setMemocheck: " +mDatabase_count);

                    // 해당 단어 즐겨찾기 목록에 추가
                    mDatabase_star = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("userVoca")
                            .child("star");
                    mDatabase_star.updateChildren(vo.toMap());
                    Log.d(TAG, "setMemocheck: " +mDatabase_count);
                } else {
                    vo.setStarCheck(false);
                    // DB 내 starCheck
                    mDatabase_star = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("userVoca").child(title)
                            .child(vo.getVocaEng())
                            .child("starCheck");
                    mDatabase_star.setValue("false");
                    Log.d(TAG, "setMemocheck: " +mDatabase_count);

                    // 해당 단어 즐겨찾기 목록에서 제거
                    mDatabase_star = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("userVoca")
                            .child("star")
                            .child(vo.getVocaEng());
                    mDatabase_star.setValue(null);
                    Log.d(TAG, "setMemocheck: " +mDatabase_count);
                }
            }
        });

        // 현재단어/전체단어 ex) 3/20
        count.setText((getAdapterPosition()%dataSize + 1) + "/" +(dataSize));

        String realAnswer=vo.getVocaEng();
        vocaKor.setText(vo.getVocaKor());
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(realAnswer.equals(editAnswerText.getText().toString())) {
                    //MediaPlayer mp=MediaPlayer.create(checkBtn.getContext(), R.raw.o);
                    //mp.start();
                    Toast.makeText(v.getContext(),"정답",Toast.LENGTH_SHORT).show();
                    editAnswerText.setText(null);
                }
                else {
                    //MediaPlayer mp=MediaPlayer.create(checkBtn.getContext(), R.raw.x);
                    //mp.start();
                    Toast.makeText(v.getContext(),"오답",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // 오늘 날짜 계산
    private String getToday()
    {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);

        String today = sdf.format(date);

        return today;
    }
}
