package ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import co.sancarbar.hampi.R
import com.squareup.picasso.Picasso
import model.Plant

/**
 * @author Santiago Carrillo
 * 7/30/18.
 */
class PlantsEntriesAdapter : RecyclerView.Adapter<PlantsEntriesAdapter.ViewHolder>() {


    private val plantsList: ArrayList<Plant> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.plant_entry_row, parent, false))
    }

    override fun getItemCount(): Int {
        return plantsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plant = plantsList[position]
        holder.name.text = plant.name
        holder.description.text = plant.description
        Picasso.get().load(plant.imageUrl).into(holder.photo)
    }

    fun add(plant: Plant) {
        plantsList.add(plant)
        notifyDataSetChanged()
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val name: TextView = view.findViewById(R.id.name)

        var description: TextView = view.findViewById(R.id.description)

        var photo: ImageView = view.findViewById(R.id.photo)
    }
}