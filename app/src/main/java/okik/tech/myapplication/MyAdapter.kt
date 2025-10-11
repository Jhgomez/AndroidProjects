package okik.tech.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import okik.tech.myapplication.databinding.RecyclerItemBinding

class MyAdapter(private val itemList: List<Int>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList.get(position))
    }


    class ViewHolder(private val binding: RecyclerItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(number: Int) {
            binding.numero.text = number.toString()
        }
    }
}