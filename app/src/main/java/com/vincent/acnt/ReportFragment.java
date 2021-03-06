package com.vincent.acnt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.vincent.acnt.adapter.ReportListAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.ReportItem;
import com.vincent.acnt.entity.SubjectType;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ReportFragment extends Fragment {
    protected Context context;

    private TextView txtBalance;

    private SubjectType subjectType;
    private List<ReportItem> reportItems;

    private ReportListAdapter adapter;

    public ReportFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        txtBalance = getView().findViewById(R.id.txtBalance);
        ListView lstReport = getView().findViewById(R.id.lstReport);

        lstReport.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(context, LedgerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constant.PRO_NAME, reportItems.get(position).getName());
                it.putExtras(bundle);
                context.startActivity(it);
            }
        });

        adapter = new ReportListAdapter(context, reportItems);
        lstReport.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        showBalance();
    }

    private void showBalance() {
        int balance = 0;
        for (int i = 0, len = reportItems.size(); i < len; i++) {
            balance += reportItems.get(i).getBalance();
        }

        String balanceTitle = "";
        switch (subjectType) {
            case ASSET:
                balanceTitle = "資產餘額：";
                break;
            case LIABILITY:
                balanceTitle = "負債餘額：";
                break;
            case CAPITAL:
                balanceTitle = "權益餘額：";
                break;
            case REVENUE:
                balanceTitle = "收入餘額：";
                break;
            case EXPENSE:
                balanceTitle = "支出餘額：";
                break;
        }

        txtBalance.setText(balanceTitle +  NumberFormat.getNumberInstance(Locale.US).format(balance));
    }

    public void setType(SubjectType subjectType) {
        this.subjectType = subjectType;
    }

    public void setReportItems(List<ReportItem> reportItems) {
        if (adapter != null) {
            adapter.setItems(reportItems);
        }

        this.reportItems = reportItems;
    }

}
