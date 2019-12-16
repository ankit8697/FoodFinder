package hu.ait.foodfinder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import hu.ait.foodfinder.adapter.FoodAdapter
import hu.ait.foodfinder.data.Food
import kotlinx.android.synthetic.main.activity_forum.*
import java.time.LocalDateTime

class ForumActivity : AppCompatActivity() {

    private lateinit var foodAdapter: FoodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum)
        setSupportActionBar(toolbar)
        fab.setOnClickListener {
            startActivity(Intent(this@ForumActivity, CreateFoodActivity::class.java))
        }
        initRecyclerView()
        queryFoods()
    }

    private fun initRecyclerView() {
        foodAdapter = FoodAdapter(this, FirebaseAuth.getInstance().currentUser!!.uid)
        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        recyclerFoods.addItemDecoration(itemDecorator)
        val linLayoutManager = LinearLayoutManager(this)
        linLayoutManager.reverseLayout = true
        linLayoutManager.stackFromEnd = true
        recyclerFoods.layoutManager = linLayoutManager
        recyclerFoods.adapter = foodAdapter
    }

    private fun queryFoods() {
        filterExpiredItems()
        val db = FirebaseFirestore.getInstance()
        val query = db.collection(getString(R.string.resource_path))
        var allFoodsListener = query.addSnapshotListener(
            object: EventListener<QuerySnapshot> {
                override fun onEvent(querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {

                    if (e != null) {
                        Toast.makeText(this@ForumActivity, "listen error: ${e.message}", Toast.LENGTH_LONG).show()
                        return
                    }

                    for (dc in querySnapshot!!.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val food = dc.document.toObject(Food::class.java)
                                foodAdapter.addFood(food, dc.document.id)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                Toast.makeText(this@ForumActivity, "update: ${dc.document.id}", Toast.LENGTH_LONG).show()
                            }
                            DocumentChange.Type.REMOVED -> {
                                foodAdapter.removeFoodByKey(dc.document.id)
                            }
                        }
                    }
                }
            }
        )
    }

    private fun filterExpiredItems() {
        val db = FirebaseFirestore.getInstance()
        val query = db.collection(getString(R.string.resource_path))
        val expiredItems = query.whereLessThan(getString(R.string.expiryTime), LocalDateTime.now().toString())
        expiredItems.get().addOnSuccessListener { documents ->
            for (document in documents) {
                query.document(
                    document.id
                ).delete()
            }
        }
    }
}
