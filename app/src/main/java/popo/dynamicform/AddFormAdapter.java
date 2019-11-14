package popo.dynamicform;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AddFormAdapter extends RecyclerView.Adapter<AddFormAdapter.ViewHolder> {
    Context context;
    ArrayList<AddForm> arrayList;

    public AddFormAdapter(Context context, ArrayList<AddForm> addForms) {
        this.context = context;
        this.arrayList = addForms;
    }

    @NonNull
    @Override
    public AddFormAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_form, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFormAdapter.ViewHolder holder, int position) {

    }

    public ArrayList<AddForm> getArrayList()
    {
        return arrayList;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        EditText edt_form;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            edt_form = itemView.findViewById(R.id.form);

            edt_form.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    System.out.println("dynamic "+getAdapterPosition());
                    AddForm add = arrayList.get(getAdapterPosition());
                    add.setUpload(charSequence + "");
                    arrayList.set(getAdapterPosition(), add);


                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }
}
