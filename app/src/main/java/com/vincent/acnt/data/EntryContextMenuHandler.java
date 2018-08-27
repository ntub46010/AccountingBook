package com.vincent.acnt.data;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vincent.acnt.EntryEditActivity;
import com.vincent.acnt.MyApp;
import com.vincent.acnt.entity.Entry;

public class EntryContextMenuHandler {
    private Context context;
    private Entry entry;
    private FirebaseFirestore db;

    public EntryContextMenuHandler(Context context, Entry entry, FirebaseFirestore db) {
        this.context = context;
        this.entry = entry;
        this.db = db;
    }

    public void updateEntry() {
        //調整日期格式
        StringBuilder date = new StringBuilder(String.valueOf(entry.getDate()));
        date.insert(4, "/");
        date.insert(7, "/");

        Intent it = new Intent(context, EntryEditActivity.class);
        Bundle bundle = new Bundle();

        bundle.putInt(Constant.KEY_MODE, Constant.MODE_UPDATE);
        bundle.putString(Constant.PRO_DATE, date.toString());
        bundle.putSerializable(Constant.KEY_ENTRY, entry);
        it.putExtras(bundle);

        context.startActivity(it);
    }

    public void deleteEntry(String dialogTitle, final ProgressBar prgBar, final RecyclerView recyEntry) {
        Utility.getPlainDialog(context, dialogTitle, "確定要刪除分錄「" + entry.getMemo() + "」嗎？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prgBar.setVisibility(View.VISIBLE);
                        recyEntry.setVisibility(View.INVISIBLE);

                        db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId()).collection(Constant.KEY_ENTRIES).document(entry.obtainDocumentId())
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "分錄刪除成功", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, "分錄刪除失敗", Toast.LENGTH_SHORT).show();
                                        }

                                        prgBar.setVisibility(View.GONE);
                                        recyEntry.setVisibility(View.VISIBLE);
                                    }
                                });
                    }
                })
                .setNegativeButton("否", null)
                .show();
    }
}
