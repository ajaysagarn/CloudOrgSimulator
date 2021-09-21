package MapReduce

import HelperUtils.{CloudSimUtils, CreateLogger}
import com.typesafe.config.Config
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.core.Simulation
import MapReduce.Mapper
import MapReduce.Reducer
import Simulations.StartSimulation
import org.cloudsimplus.listeners.CloudletEventInfo

import collection.JavaConverters.*

import java.util.UUID.randomUUID

class MapReduceJob(simulation: Simulation, broker: DatacenterBroker, config: Config) {

  val logger = CreateLogger(classOf[MapReduceJob])
  // The total size of the data to be processed
  val dataSize = config.getInt("TotalDataSizeMbs")
  logger.info("Creating a Map reduce job with data size = "+dataSize)
  // The max size of each split.
  val splitSize = config.getInt("SplitSizeMbs")
  // The number of map-reduce instances that need to be generated.
  val jobCount: Int = dataSize/splitSize

  logger.info("No of mapper and reducer instances = "+jobCount)

  // A map to store a collection of mappers and their respective reducer instances.
  //Reducer cloudlet will be submitted once a corresponding mapper cloudlet is done executing.
  val mapperReduceMap =  Map.empty[Long,Reducer]

  val mappers = createMappers()



  def submitJob(): Unit = {
      logger.info("Submitting mappers to broker")
      broker.submitCloudletList(mappers.asJava)
  }

  def createMappers(): List[Mapper] = {
    val mappers = List.fill(jobCount)(new Mapper(splitSize,config.getInt("MapperPe")))
    var map_count = 0
    mappers.foreach(mapper => {
      val map_id = randomUUID().getMostSignificantBits()
      mapper.setId(map_count)
      mapper.setUtilizationModelRam(CloudSimUtils.getUtilizationModel(config.getString("MapperUtilizationModelRam")))
      mapper.setUtilizationModelBw(CloudSimUtils.getUtilizationModel(config.getString("MapperUtilizationModelBw")))
      mapper.setUtilizationModelCpu(CloudSimUtils.getUtilizationModel(config.getString("MapperUtilizationModelCpu")))
      mapper.addOnFinishListener(this.sendReducerJobs)

      map_count = map_count + 1
      logger.info("Created mapper cloudlet with id "+map_count)


      val reducer = new Reducer(splitSize,config.getInt("ReducerPe"))
      reducer.setUtilizationModelRam(CloudSimUtils.getUtilizationModel(config.getString("ReducerUtilizationModelRam")))
      reducer.setUtilizationModelBw(CloudSimUtils.getUtilizationModel(config.getString("ReducerUtilizationModelBw")))
      reducer.setUtilizationModelCpu(CloudSimUtils.getUtilizationModel(config.getString("ReducerUtilizationModelCpu")))

      logger.info("Created reducer cloudlet with id"+reducer.getId)

      addMapperReducerMapping(map_id, reducer)
    })
    mappers
  }

  def addMapperReducerMapping(maper_id: Long,reducer: Reducer): Unit = {
    mapperReduceMap + (maper_id->reducer)
  }

  def getMappers(): List[Mapper] = mappers

  def sendReducerJobs(evt: CloudletEventInfo): Unit ={
      val mapper = evt.getCloudlet()

      val reducer: Option[Reducer]  = mapperReduceMap.get(mapper.getId)

      logger.info("Subbmitting new reducer instance to broker")

      broker.submitCloudlet(reducer.getOrElse(new Reducer(splitSize,config.getInt("ReducerPe"))
        .setUtilizationModelRam(CloudSimUtils.getUtilizationModel(config.getString("ReducerUtilizationModelRam")))
      .setUtilizationModelBw(CloudSimUtils.getUtilizationModel(config.getString("ReducerUtilizationModelBw")))
      .setUtilizationModelCpu(CloudSimUtils.getUtilizationModel(config.getString("ReducerUtilizationModelCpu")))))
  }


}
