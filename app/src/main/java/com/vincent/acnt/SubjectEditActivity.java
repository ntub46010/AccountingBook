package com.vincent.acnt;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.data.Verifier;
import com.vincent.acnt.entity.Subject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class SubjectEditActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "會計科目";
    private Bundle bundle;
    private FirebaseFirestore db;

    private ScrollView layout;
    private Spinner spnType;
    private EditText edtNo, edtName, edtCredit, edtDebit;
    private ProgressBar prgBar;

    private List<Integer> subjectNos;
    private String documentId;

    private Dialog dlgWaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_edit);
        context = this;
        db = MyApp.getInstance().getFirestore();
        bundle = getIntent().getExtras();

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView txtBarTitle = toolbar.findViewById(R.id.txtToolbarTitle);
        txtBarTitle.setText(activityTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView btnSubmit = findViewById(R.id.btnSubmit);
        layout = findViewById(R.id.layout);
        spnType = findViewById(R.id.spnSubjectType);
        edtNo = findViewById(R.id.edtSubjectNo);
        edtName = findViewById(R.id.edtSubjectName);
        edtCredit = findViewById(R.id.edtCredit);
        edtDebit = findViewById(R.id.edtDebit);
        prgBar = findViewById(R.id.prgBar);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String credit = edtCredit.getText().toString();
                String debit = edtDebit.getText().toString();

                Subject subject = new Subject();
                subject.setId(System.currentTimeMillis());
                subject.setNo(String.valueOf(spnType.getSelectedItemPosition()) + edtNo.getText().toString());
                subject.setName(edtName.getText().toString());
                subject.setCredit(credit.equals("") ? 0 : Integer.parseInt(credit));
                subject.setDebit(debit.equals("") ? 0 : Integer.parseInt(debit));

                if (isNotValid(subject)) {
                    return;
                }

                dlgWaiting.show();

                if (bundle.getInt(Constant.KEY_MODE) == Constant.MODE_CREATE) {
                    createSubject(subject);
                } else {
                    subject.defineDocumentId(documentId);
                    updateSubject(subject);
                }
            }
        });

        if (bundle.getInt(Constant.KEY_MODE) == Constant.MODE_UPDATE) {
            Subject subject = (Subject) bundle.getSerializable(Constant.KEY_SUBJECT);
            documentId = subject.obtainDocumentId();
            spnType.setSelection(Integer.parseInt(subject.getNo().substring(0, 1)));
            edtNo.setText(subject.getNo().substring(1, 3));
            edtName.setText(subject.getName());
            edtCredit.setText(String.valueOf(subject.getCredit()));
            edtDebit.setText(String.valueOf(subject.getDebit()));
        }

        dlgWaiting = Utility.getWaitingDialog(context);

        loadSubjectNos();
    }

    private void loadSubjectNos() {
        layout.setVisibility(View.INVISIBLE);

        db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId()).collection(Constant.KEY_SUBJECTS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        subjectNos = new ArrayList<>();

                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                            subjectNos.add(Integer.parseInt(documentSnapshots.get(i).toObject(Subject.class).getNo()));
                        }

                        prgBar.setVisibility(View.GONE);
                        layout.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void createSubject(Subject subject) {
        db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId()).collection(Constant.KEY_SUBJECTS)
                .add(subject)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        dlgWaiting.dismiss();

                        if (task.isSuccessful()) {
                            Toast.makeText(context, "科目新增成功", Toast.LENGTH_SHORT).show();

                            spnType.setSelection(0);
                            edtNo.setText(null);
                            edtName.setText(null);
                            edtCredit.setText(null);
                            edtDebit.setText(null);
                            return;
                        }

                        Toast.makeText(context, "科目新增失敗", Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateSubject(Subject subject) {
        db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId()).collection(Constant.KEY_SUBJECTS).document(subject.obtainDocumentId())
                .set(subject)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dlgWaiting.dismiss();

                        if (task.isSuccessful()) {
                            Toast.makeText(context, "科目編輯成功", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        Toast.makeText(context, "科目編輯失敗", Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean isNotValid(Subject subject) {
        StringBuilder errMsg = new StringBuilder(64);
        Verifier v = new Verifier(context);

        if (subject.getNo().substring(0, 1).equals("0")) {
            errMsg.append("科目類別未選擇\n");
        }

        if (bundle.getInt(Constant.KEY_MODE) == Constant.MODE_CREATE) {
            if (subjectNos.indexOf(Integer.parseInt(subject.getNo())) >= 0) {
                errMsg.append("科目編號").append(subject.getNo()).append("已被使用\n");
            }
        }

        errMsg.append(v.chkSubjectNo(String.valueOf(subject.getNo())));
        errMsg.append(v.chkSubjectName(subject.getName()));

        if (subject.getCredit() != 0 && subject.getDebit() != 0) {
            errMsg.append("只能在借貸其中一方輸入金額\n");
        }

        if (errMsg.length() != 0) {
            Utility.getPlainDialog(context, activityTitle, errMsg.toString()).show();
            return true;
        }

        return false;
    }
}
