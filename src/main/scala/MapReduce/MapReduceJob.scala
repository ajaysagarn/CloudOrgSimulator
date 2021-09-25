package MapReduce

import HelperUtils.{CloudSimUtils, CreateLogger}
import com.typesafe.config.Config
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.core.Simulation
import MapReduce.Mapper
import MapReduce.Reducer
import Simulations.StartSimulation
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudsimplus.listeners.CloudletEventInfo

import collection.JavaConverters.*
import java.util.UUID.randomUUID

/**
 * This class is responsible for creating and submitting Mappers and Reducers to a simulation broker.
 * @param simulation - An instance of Cloudsim simulation
 * @param broker - The broker used in the simulation
 * @param config - THe configuration based on which Mappers and Reducers are created
 * @param targetDC - [Optional] The target datacenter to which a Mapper/Reducer cloudlet needs to be assigned to
 */
class MapReduceJob(simulation: Simulation, broker: DatacenterBroker, config: Config, targetDC:Option[NetworkDatacenter] = Option.empty ) {

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

  //Create a list of required mappers
  val mappers = createMappers()

  /**
   * Submit the Mappers to the simulation broker
   */
  def submitJob(): Unit = {
      logger.info("Submitting mappers to broker")
      broker.submitCloudletList(mappers.asJava)
  }

  /**
   * Create a List of mappers based on the config parameters
   * @return List[Mapper]
   */
  def createMappers(): List[Mapper] = {
    val mappers = List.fill(jobCount)(createMapper())

    var map_count = 0
    mappers.foreach(mapper => {
      val map_id = randomUUID().getMostSignificantBits()
      mapper.setId(map_count)
      map_count = map_count + 1

      logger.info("Created mapper cloudlet with id "+map_count)

      val reducer = createReducer()

      //Add an on finish listener to the mapper. This is used to trigger the creation of a reducer when the mappoer finishes execution
      mapper.addOnFinishListener(this.sendReducerJobs)

      logger.info("Created reducer cloudlet with id"+reducer.getId)

      //Add the reducer to the map. This reducer will be submitted to the broker/datacenter when the corresponding mapper is done executing
      addMapperReducerMapping(map_id, reducer)

      if(!targetDC.isEmpty){
        mapper.assignToDatacenter(targetDC.get)
      }

    })

    mappers
  }

  /**
   * Add the reducer to the map. This reducer will be submitted to the broker/datacenter when the corresponding mapper is done executing
   * @param maper_id - the id of the mapper cloudlet
   * @param reducer - the corresponding reducer cloudlet
   */
  def addMapperReducerMapping(maper_id: Long,reducer: Reducer): Unit = {
    mapperReduceMap + (maper_id->reducer)
  }

  def getMappers(): List[Mapper] = mappers
  def getReducers(): List[Reducer] = mapperReduceMap.values.toList

  /**
   * Sends the reducer to the broker/datacenter
   * @param evt
   */
  def sendReducerJobs(evt: CloudletEventInfo): Unit ={
      val mapper: Mapper = evt.getCloudlet().asInstanceOf[Mapper]
      val reducer: Reducer  = mapperReduceMap.get(mapper.getId).getOrElse(createReducer())

      if(!targetDC.isEmpty){
        reducer.assignToDatacenter(targetDC.get)
      }

  }

  /**
   * Create a single mapper based on the configuration
   * @return
   */
  def createMapper(): Mapper = {
    val mapper = new Mapper(splitSize,config.getInt("MapperPe"))
    mapper.setUtilizationModelRam(CloudSimUtils.getUtilizationModel(config.getString("MapperUtilizationModelRam")))
    mapper.setUtilizationModelBw(CloudSimUtils.getUtilizationModel(config.getString("MapperUtilizationModelBw")))
    mapper.setUtilizationModelCpu(CloudSimUtils.getUtilizationModel(config.getString("MapperUtilizationModelCpu")))
    mapper
  }

  /**
   * Create a single reducer based on the configuration
   * @return
   */
  def createReducer(): Reducer = {
    val reducer = new Reducer(splitSize,config.getInt("ReducerPe"))
    reducer.setUtilizationModelRam(CloudSimUtils.getUtilizationModel(config.getString("ReducerUtilizationModelRam")))
    reducer.setUtilizationModelBw(CloudSimUtils.getUtilizationModel(config.getString("ReducerUtilizationModelBw")))
    reducer.setUtilizationModelCpu(CloudSimUtils.getUtilizationModel(config.getString("ReducerUtilizationModelCpu")))
    reducer
  }


}
