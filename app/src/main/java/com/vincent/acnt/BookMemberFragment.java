package com.vincent.acnt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vincent.acnt.adapter.MemberListAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.User;

import java.util.List;

public class BookMemberFragment extends Fragment {
    private Context context;

    private List<User> members;
    private RelativeLayout layHint;

    private MemberListAdapter adapter;
    private int type;

    private Dialog dialog;

    public BookMemberFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        return inflater.inflate(R.layout.fragment_member, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        layHint = getView().findViewById(R.id.layContentHint);

        if (type == Constant.CODE_WAITING && members.isEmpty()) {
            TextView txtHint = getView().findViewById(R.id.txtHint);
            txtHint.setText("沒有待批准的使用者");
            layHint.setVisibility(View.VISIBLE);
        } else {
            layHint.setVisibility(View.GONE);
        }

        ListView lstMember = getView().findViewById(R.id.lstMember);
        adapter = new MemberListAdapter(getActivity(), type, members);

        lstMember.setAdapter(adapter);
        lstMember.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (type == Constant.CODE_WAITING) {
                    dialog = prepareAdmissionDialog(position).show();
                } else {
                    dialog = prepareNormalDialog(position).show();
                }
            }
        });
    }

    public void setMembers(List<User> members) {
        if (adapter != null) {
            adapter.setMembers(members);
        }
        this.members = members;
    }

    public void setType(int type) {
        this.type = type;
    }

    public MemberListAdapter getAdapter() {
        return adapter;
    }

    private AlertDialog.Builder prepareAdmissionDialog(final int index) {
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.dlg_member_operation, null);

        ImageView imgProfile = layout.findViewById(R.id.imgProfile);
        TextView txtMemberName = layout.findViewById(R.id.txtMemberName);
        Button btnPositive = layout.findViewById(R.id.btnPositive);
        Button btnNegative = layout.findViewById(R.id.btnNegative);

        txtMemberName.setText(members.get(index).getName());

        btnPositive.setText("接受");
        btnPositive.setTextColor(Color.parseColor("#00AA00"));
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.approveUser(members.get(index));
                dialog.dismiss();
            }
        });

        btnNegative.setText("拒絕");
        btnNegative.setTextColor(Color.parseColor("#AA0000"));
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.rejectUser(members.get(index));
                dialog.dismiss();
            }
        });

        return new AlertDialog.Builder(context)
                .setView(layout)
                .setCancelable(true);
    }

    private AlertDialog.Builder prepareNormalDialog(final int index) {
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.dlg_member_operation, null);

        ImageView imgProfile = layout.findViewById(R.id.imgProfile);
        TextView txtMemberName = layout.findViewById(R.id.txtMemberName);
        Button btnPositive = layout.findViewById(R.id.btnPositive);
        Button btnNegative = layout.findViewById(R.id.btnNegative);

        User user = members.get(index);

        txtMemberName.setText(user.getName());

        if (user.getName().equals(MyApp.user.getName()) || !MyApp.browsingBook.isAdminUser(MyApp.user.getId())) {
            btnPositive.setVisibility(View.GONE);
            btnNegative.setVisibility(View.GONE);

            return new AlertDialog.Builder(context)
                    .setView(layout)
                    .setCancelable(true);
        }

        if (MyApp.browsingBook.isAdminUser(user.getId())) {
            btnPositive.setText("移除管理員");
            btnPositive.setTextColor(Color.parseColor("#00AA00"));
            btnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.getPlainDialog(context, "成員", "確定要移除" + members.get(index).getName() + "的管理員身份嗎？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    adapter.degradeUser(members.get(index));
                                }
                            })
                            .setNegativeButton("否", null)
                            .show();

                    dialog.dismiss();
                }
            });
        } else {
            btnPositive.setText("設為管理員");
            btnPositive.setTextColor(Color.parseColor("#00AA00"));
            btnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.getPlainDialog(context, "成員", "確定要將" + members.get(index).getName() + "升為管理員嗎？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    adapter.upgradeUser(members.get(index));
                                }
                            })
                            .setNegativeButton("否", null)
                            .show();

                    dialog.dismiss();
                }
            });
        }

        btnNegative.setText("移除成員");
        btnNegative.setTextColor(Color.parseColor("#AA0000"));
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.getPlainDialog(context, "成員", "確定要移除" + members.get(index).getName() + "嗎？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.removeUser(members.get(index));
                        }
                    })
                    .setNegativeButton("否", null)
                    .show();

                dialog.dismiss();
            }
        });

        return new AlertDialog.Builder(context)
                .setView(layout)
                .setCancelable(true);
    }
}