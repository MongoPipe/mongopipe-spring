package org.test;

import org.mongopipe.core.annotation.PipelineRun;
import org.mongopipe.core.annotation.Store;

import java.util.stream.Stream;

@Store
public interface OrderReports {
  @PipelineRun("totalOrdersGroupedBySize")
  Stream<Order> getTotalOrders(String size);
}
