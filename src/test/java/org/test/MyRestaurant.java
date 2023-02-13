package org.test;

import org.mongopipe.core.annotation.Item;
import org.mongopipe.core.annotation.PipelineRun;
import org.mongopipe.core.annotation.Store;

import java.util.Optional;
import java.util.stream.Stream;

@Store(
    // "items" mapping relation field is NOT required if using only @PipelineRun methods.
    // It is required only when using CRUD methods (without @PipelineRun), because POJO classes are store agnostic.
    items = {
        @Item(type = Pizza.class, collection = "pizzas")
    }
)
public interface MyRestaurant {
  @PipelineRun("matchingPizzas")  // your pipeline id, copied from matchingPizzasBySize.bson, method can have any name.
  Stream<Pizza> getPizzasBySize(String pizzaSize);

  Optional<Pizza> findById(String id); // For more CRUDs methods see org.mongopipe.core.store.CrudStore.

}
