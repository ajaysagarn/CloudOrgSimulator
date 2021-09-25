package Simulations

import CloudSimWrappers.{ConfiguredBroker, ConfiguredVm}
import HelperUtils.CloudSimUtils
import MapReduce.MapReduceJob
import org.cloudbus.cloudsim.core.CloudSim
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Test suite to test if the Mapreduce job is created as per the configuration provided
 */
class MapreduceJobTestSuite extends AnyFlatSpec with Matchers{
  val config = CloudSimUtils.getconfigValue("mapreduce","MapReduceJob")
  val configDc = CloudSimUtils.getconfigValue("Saas","saasDataCenter")
  val testSim = new CloudSim()

  behavior of "MapReduceJob module"

  "MappersCreated" should "be of the the same length as" in {
    val broker = new ConfiguredBroker(testSim, configDc).getConfiguredBroker()
    val job = new MapReduceJob(testSim, broker, config)
    val mappers = job.getMappers()
    assert(mappers.size == config.getInt("TotalDataSizeMbs")/config.getInt("SplitSizeMbs"))
  }

  "MapReducersCreated" should "have config properties" in {
    val broker = new ConfiguredBroker(testSim, configDc).getConfiguredBroker()
    val job = new MapReduceJob(testSim, broker, config)
    val mappers = job.getMappers()
    val reducers = job.getReducers()

    mappers.foreach(mapper => {
      assert(mapper.getNumberOfPes == config.getLong("MapperPe")) //verify the mapper pes
    })

    reducers.foreach(reducer => {
      assert(reducer.getNumberOfPes == config.getLong("ReducerPe")) //verify the reducer pes
    })
  }

  "MapReduceJob" should "submit mappers to broker" in {
    val broker = new ConfiguredBroker(testSim, configDc).getConfiguredBroker()
    val job = new MapReduceJob(testSim, broker, config)
    val mappers = job.getMappers()
    job.submitJob()

    assert(broker.getCloudletSubmittedList.size() == mappers.size) //check if all mappers have been submitted to the broker
  }

}
