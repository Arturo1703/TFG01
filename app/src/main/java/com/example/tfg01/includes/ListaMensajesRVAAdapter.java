package com.example.tfg01.includes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg01.R;
import com.example.tfg01.modelos.Mensaje;

import java.util.ArrayList;

public class ListaMensajesRVAAdapter extends RecyclerView.Adapter<ListaMensajesRVAAdapter.ViewHolder> {

    private ArrayList<Mensaje> listaMensajes = new ArrayList<>();

    private Context context;
    OnItemClickListener miListener;

    public ListaMensajesRVAAdapter(Context context){
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cv_mensaje, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    public interface OnItemClickListener {
        void onItemClick(String id, Integer num);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        miListener = listener;
    }

    //Esta funcion nos permite crear un comportamiento al clicar en una tarjeta. En nuestro caso llama a un listener que hemos creado anteriormente
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtName.setText(listaMensajes.get(position).getDestinatario());
        holder.txtMensaje.setText(listaMensajes.get(position).getMensaje());
        holder.txtTiempo.setText(listaMensajes.get(position).getFecha());
        //holder.image.setImageResource(listaMensajes.get(position).getImage());
        holder.botonResponder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String id = listaMensajes.get(holder.getAdapterPosition()).getId();
                miListener.onItemClick(id, 1);
            }
        });
        holder.botonBorrar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String id = listaMensajes.get(holder.getAdapterPosition()).getId();
                miListener.onItemClick(id, 2);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMensajes.size();
    }

    public void setListaMensajes (ArrayList<Mensaje> listaMensajes){
        this.listaMensajes = listaMensajes;
    }

    //Aqui es donde se asigna los aprametros que le llegan de los hijos a la infomracion que se mostrará en las tarjetas
    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtName, txtTiempo, txtMensaje;
        private Button botonResponder, botonBorrar;
        private CardView card;
        private ImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.nmlistaMensaje);
            txtTiempo = itemView.findViewById(R.id.tmplistaMensaje);
            txtMensaje = itemView.findViewById(R.id.msglistaMensaje);
            card = itemView.findViewById(R.id.cardMensajeId);
            image = itemView.findViewById(R.id.pflistaMensaje);
            botonResponder = itemView.findViewById(R.id.responderMensaje);
            botonBorrar = itemView.findViewById(R.id.borrarMensaje);

        }
    }

}
