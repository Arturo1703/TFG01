package com.example.tfg01.includes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg01.R;
import com.example.tfg01.modelos.Hijo;

import java.util.ArrayList;

//Este es un Recycler view encargado de visualizar la lista de hijos a mostrar en la apgina principal del padre
public class ListaHijosRVAdapter extends RecyclerView.Adapter<ListaHijosRVAdapter.ViewHolder> {

    private ArrayList<Hijo> listahijos = new ArrayList<>();

    private Context context;
    OnItemClickListener miListener;

    public ListaHijosRVAdapter(Context context){
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cv_hijo, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    public interface OnItemClickListener {
        void onItemClick(String id);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        miListener = listener;
    }

    //Esta funcion nos permite crear un comportamiento al clicar en una tarjeta. En nuestro caso llama a un listener que hemos creado anteriormente
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtName.setText(listahijos.get(position).getNombre());
        holder.txtId.setText(listahijos.get(position).getId());
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = listahijos.get(holder.getAdapterPosition()).getId();
                miListener.onItemClick(id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listahijos.size();
    }

    public void setListahijos (ArrayList<Hijo> listahijos){
        this.listahijos = listahijos;
    }

    //Aqui es donde se asigna los aprametros que le llegan de los hijos a la infomracion que se mostrará en las tarjetas
    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtName, txtId;
        private CardView card;
        private ImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.nmlistahijo);
            txtId = itemView.findViewById(R.id.idlistahijo);
            card = itemView.findViewById(R.id.cardHijoId);
            image = itemView.findViewById(R.id.pflistahijo);
        }
    }

}
