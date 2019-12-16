package hu.ait.foodfinder.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hu.ait.foodfinder.ForumActivity
import hu.ait.foodfinder.InformationActivity
import hu.ait.foodfinder.R
import hu.ait.foodfinder.data.Food
import kotlinx.android.synthetic.main.food_item_row.view.*

class FoodAdapter(private val context: Context, private val uid: String) : RecyclerView.Adapter<FoodAdapter.ViewHolder>() {

    private var foodList = mutableListOf<Food>()
    private var foodKeys = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.food_item_row, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val food = foodList[position]

        holder.tvName.text = food.name
        holder.tvLocation.text = food.location

        if (food.uid == uid) {
            holder.btnDelete.visibility = View.VISIBLE
        } else {
            holder.btnDelete.visibility = View.GONE
        }

        holder.btnDelete.setOnClickListener {
            removeFood(holder.adapterPosition)
            val storageRef = FirebaseStorage.getInstance().reference
            val newImageRef = storageRef.child("images/${food.imageUrl}")
            newImageRef.delete()
        }

        holder.row.setOnClickListener {
            val intent = Intent()
            intent.putExtra("KEY_FOOD", food)
            intent.setClass(context, InformationActivity::class.java)
            (context as ForumActivity).startActivity(intent)
        }
    }

    fun addFood(food : Food, key : String) {
        foodList.add(food)
        foodKeys.add(key)
        notifyDataSetChanged()
    }

//    Used when the author deletes his post
    private fun removeFood(index: Int) {
        FirebaseFirestore.getInstance().collection("foods").document(
            foodKeys[index]
        ).delete()

        foodList.removeAt(index)
        foodKeys.removeAt(index)
        notifyItemRemoved(index)
    }

//  Used when someone else delete's their post
    fun removeFoodByKey(key: String) {
        val index = foodKeys.indexOf(key)
        if (index != -1) {
            foodList.removeAt(index)
            foodKeys.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName = itemView.tvName
        var tvLocation = itemView.tvLocation
        var btnDelete = itemView.btnDelete
        var row = itemView
    }
}