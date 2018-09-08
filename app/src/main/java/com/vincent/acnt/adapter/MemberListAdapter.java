package com.vincent.acnt.adapter;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.vincent.acnt.MyApp;
import com.vincent.acnt.R;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.User;

import java.util.List;

public class MemberListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private int type;
    public int clickIndex;
    private List<User> members;

    public MemberListAdapter(Context context, int type, List<User> members) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.type = type;
        this.members = members;
    }

    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Object getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.lst_member, parent, false);
        }

        ImageView imgProfile = view.findViewById(R.id.imgProfile);
        TextView txtMemberName = view.findViewById(R.id.txtMemberName);
        ImageView imgMgr = view.findViewById(R.id.imgMgr);

        User user = members.get(position);
        txtMemberName.setText(user.getName());

        if (MyApp.browsingBook.isAdmin(user.getId())) {
            imgMgr.setVisibility(View.VISIBLE);
        } else {
            imgMgr.setVisibility(View.GONE);
        }

        return view;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public void approveUser(final User user) {
        MyApp.browsingBook.removeWaitingMember(user.getId());
        MyApp.browsingBook.getApprovedMembers().add(user);

        MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .set(MyApp.browsingBook)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "已加入" + user.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void rejectUser(final User user) {
        MyApp.browsingBook.removeWaitingMember(user.getId());

        MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .set(MyApp.browsingBook)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "已拒絕" + user.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void upgradeUser(final User user) {
        MyApp.browsingBook.removeApprovedMember(user.getId());
        MyApp.browsingBook.addAdminMember(user);

        MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .set(MyApp.browsingBook)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "已給予" + user.getName() + "管理員身份", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void degradeUser(final User user) {
        MyApp.browsingBook.removeAdminMember(user.getId());
        MyApp.browsingBook.addApprovedMember(user);

        MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .set(MyApp.browsingBook)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "已移除"  + user.getName() + "的管理員身份", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void removeUser(final User user) {
        MyApp.browsingBook.removeApprovedMember(user.getId());
        MyApp.browsingBook.removeAdminMember(user.getId());

        MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .set(MyApp.browsingBook)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "已移除" + user.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
