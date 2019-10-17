package com.lhg1304.onimani.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.lhg1304.onimani.R;
import com.lhg1304.onimani.views.transitions.FabTransform;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddFriendActivity extends AppCompatActivity {

    @BindView(R.id.username)
    AutoCompleteTextView tvEmail;

    @BindView(R.id.btn_find)
    Button btnFind;

    private InputMethodManager imm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_friend);
        ButterKnife.bind(this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        FabTransform.setup(this, findViewById(R.id.search_container));

        initButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initButton() {
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doneButton();
            }
        });
        tvEmail.setImeOptions(EditorInfo.IME_ACTION_DONE);
        tvEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if ( actionId == EditorInfo.IME_ACTION_DONE ) {
                    doneButton();
                    return true;
                }
                return false;
            }
        });
    }

    private void doneButton() {
        hideKeyboard();
        String inputString = tvEmail.getText().toString();

        if ( inputString.isEmpty() ) {
            Snackbar.make(tvEmail, "이메일을 입력해주세요.", Snackbar.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("email", inputString);
        setResult(RESULT_OK, intent);
        finishAfterTransition();
    }

    private void hideKeyboard() {
        imm.hideSoftInputFromWindow(tvEmail.getWindowToken(), 0);
    }

    public void dismiss(View view) {
        hideKeyboard();
        finishAfterTransition();
    }
}
