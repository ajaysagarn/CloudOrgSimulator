package CloudSimWrappers

import HelperUtils.{CloudSimUtils, CreateLogger}
import com.typesafe.config.Config
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.core.Simulation
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.resources.{Processor, Ram}
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.vms.network.NetworkVm
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudsimplus.autoscaling.resources.ResourceScalingInstantaneous
import org.cloudsimplus.autoscaling.{HorizontalVmScaling, HorizontalVmScalingSimple, VerticalVmScaling, VerticalVmScalingSimple}
import org.cloudsimplus.listeners.EventInfo

import java.util.function.Supplier
import collection.JavaConverters.*
import scala.::

/**
 * Wrapper class to create @Link{NetworkVm}  for a simulatio based on the configuration passed
 * @param simulation - An instance of the cloud sim simulation
 * @param broker - The broker created for the simulation
 * @param config - The configuration to be used to create the Vms
 */
class ConfiguredVm(simulation: Simulation,broker: DatacenterBroker, config: Config) {

  val logger = CreateLogger(classOf[ConfiguredVm])
  //create the list of vms
  var vms:List[NetworkVm] = createVms()

  /**
   *  Create the vms based on the configuration values
   * @return List[NetworkVm]
   */
  def createVms(): List[NetworkVm] = {
    val vms = List.fill(config.getInt("vm.InitialCount"))(new NetworkVm(config.getLong("vm.mipsCapacity"), config.getInt("vm.Pes")))
    vms.foreach(vm =>{
      vm.setRam(config.getInt("vm.RAMInMBs"))
      vm.setBw(config.getInt("vm.BandwidthInMBps"))
      vm.setSize(config.getInt("vm.StorageInMMapBs"))
      vm.setCloudletScheduler(CloudSimUtils.getCloudletScheduler(config.getString("vm.CloudletSchedulerType")))
      vm.setSubmissionDelay(config.getInt("vm.DefaultSubmissionDelay"))

      // check if vertical CPU scaling is enabled. If so set the scaling function
      if(config.hasPath("vm.VerticalCpuScalingEnabled") && config.getBoolean("vm.VerticalCpuScalingEnabled")){
        vm.setPeVerticalScaling(this.createVerticalPeScaling)
      }

      // check if vertical RAM scaling is enabled. If so set the scaling function
      if(config.hasPath("vm.VerticalRamScalingEnabled") && config.getBoolean("vm.VerticalRamScalingEnabled")) {
        vm.setRamVerticalScaling(this.createVerticalRamScaling)
      }

      // check if horizontal VM scaling is enabled. If so set the scaling function
      if(config.hasPath("vm.HorizontalScalingEnabled") && config.getBoolean("vm.HorizontalScalingEnabled")) {
        vm.setHorizontalScaling(this.createHorizontalVmScaling)
      }
    })
    //set the clock listener that listenes to the vms every second. This is used to collect execution logs of vms
    simulation.addOnClockTickListener(this.onClockTickListener)
    vms
  }

  /**
   * Get the vms created
   * @return List[networkVm]
   */
  def getVms: List[NetworkVm] = vms

  /**
   * Submits all the vms created to the broker instance passed during class creation
   */
  def submitinitialVms(): Unit ={
    broker.submitVmList(vms.asJava)
  }

  /**
   * Apply specific customer input changes to the vms. These are changes that an external IAAS or PAAS customer can make
   * @param custconfig
   */
  def applyCustomerChanges(custconfig: Config): Unit = {
    // increate the number of vms if current config is greater than the default
    if(vms.size < custconfig.getInt("InitialCount")){
      val additionalVms: List[NetworkVm] = List.fill(custconfig.getInt("InitialCount") - vms.size)(createVm)
      vms = vms ::: additionalVms
    }

    //set the new required parameters for all of the vms created
    vms.foreach(vm => {
      vm.setRam(custconfig.getInt("RAMInMBs"))
      vm.setBw(custconfig.getInt("BandwidthInMBps"))
      vm.setSize(custconfig.getInt("StorageInMMapBs"))

      if(custconfig.hasPath("VerticalCpuScalingEnabled") && custconfig.getBoolean("VerticalCpuScalingEnabled")){
        vm.setPeVerticalScaling(this.createVerticalPeScaling)
      }

      if(custconfig.hasPath("VerticalRamScalingEnabled") && custconfig.getBoolean("VerticalRamScalingEnabled")) {
        vm.setRamVerticalScaling(this.createVerticalRamScaling)
      }

      if(custconfig.hasPath("HorizontalScalingEnabled") && custconfig.getBoolean("HorizontalScalingEnabled")) {
        vm.setHorizontalScaling(this.createHorizontalVmScaling)
      }

    })

  }

  /**
   * Extract vm logs for each clock tick during simulation
   * @param evt
   */
  def onClockTickListener(evt: EventInfo): Unit = {
    //perform some logging here to print current vm and host utilization metrics
    vms.foreach(vm => {
      val time = evt.getTime
      val vmid = vm.getId
      val cpuUtil = vm.getCpuPercentUtilization() * 100.0
      val noPes = vm.getNumberOfPes()
      val cloudlets = vm.getCloudletScheduler.getCloudletExecList.size()
      val ramutilPercent = vm.getRam.getPercentUtilization() * 100
      val ramAllocation = vm.getRam.getAllocatedResource()
      val log = f"Time $time: Vm $vmid CPU Usage:$cpuUtil percentage ($noPes vCPUs. Running Cloudlets: #$cloudlets). RAM usage: $ramutilPercent  ($ramAllocation MB) \n"
      logger.trace(log)
    })
  }

  /**
   * instantiate a horizontalVMScaling class
   * @return HorizontalVmScalingSimple
   */
  private def createHorizontalVmScaling = {
    val horizontalScaling = new HorizontalVmScalingSimple
    horizontalScaling.setVmSupplier(() => this.createVm).setOverloadPredicate(this.isVmOverloaded)
    horizontalScaling
  }

  /**
   * Create a single Vm based on the config
   * @return NetworkVm
   */
  private def createVm = {
    val vm = new NetworkVm(config.getLong("vm.mipsCapacity"), config.getInt("vm.Pes"))
    vm.setRam(config.getInt("vm.RAMInMBs"))
    vm.setBw(config.getInt("vm.BandwidthInMBps"))
    vm.setSize(config.getInt("vm.StorageInMMapBs"))
    vm.setCloudletScheduler(CloudSimUtils.getCloudletScheduler(config.getString("vm.CloudletSchedulerType")))
    vm.setSubmissionDelay(config.getInt("vm.DefaultSubmissionDelay"))
    vm
  }

  /**
   * Create a VerticalPeScaling class instance
   * @return
   */
  private def createVerticalPeScaling = { //The percentage in which the number of PEs has to be scaled
    val scalingFactor = config.getDouble("vm.VerticalCpuScaling.ScalingFactor")
    val verticalCpuScaling = new VerticalVmScalingSimple(classOf[Processor], scalingFactor)

    verticalCpuScaling.setResourceScaling((vs: VerticalVmScaling) => 2 * vs.getScalingFactor * vs.getAllocatedResource)
    verticalCpuScaling.setLowerThresholdFunction(this.lowerCpuUtilizationThreshold)
    verticalCpuScaling.setUpperThresholdFunction(this.upperCpuUtilizationThreshold)
    verticalCpuScaling
  }

  /**
   * define the lowercpuutilization
   * @param vm
   * @return
   */
  def lowerCpuUtilizationThreshold(vm: Vm): Double = {
    config.getDouble("vm.VerticalCpuScaling.LowerCpuUtilizationThreshold")
  }

  /**
   * define the upperCpuUtilization
   * @param vm
   * @return
   */
  def upperCpuUtilizationThreshold(vm: Vm): Double = {
    config.getDouble("vm.VerticalCpuScaling.UpperCpuUtilizationThreshold")
  }

  /**
   * create an instance of VerticalramScalingSimple class
   * @return
   */
  private def createVerticalRamScaling = { //The percentage in which the number of PEs has to be scaled
    val scalingFactor = config.getDouble("vm.VerticalRamScaling.ScalingFactor")
    val verticalRamScaling = new VerticalVmScalingSimple(classOf[Ram], scalingFactor)
    
    verticalRamScaling.setLowerThresholdFunction(this.lowerRamUtilizationThreshold)
    verticalRamScaling.setUpperThresholdFunction(this.upperRamUtilizationThreshold)
    verticalRamScaling
  }

  /**
   * define the lowerRamUtilization
   * @param vm
   * @return
   */
  def lowerRamUtilizationThreshold(vm: Vm): Double = {
    config.getDouble("vm.VerticalRamScaling.LowerRamUtilizationThreshold")
  }

  /**
   * define the upperRamUtilization
   * @param vm
   * @return
   */
  def upperRamUtilizationThreshold(vm: Vm): Double = {
    config.getDouble("vm.VerticalRamScaling.UpperRamUtilizationThreshold")
  }

  /**
   * checks if the VM cpu utilization is greater than the upper threshold specified in the config
   * @param vm
   * @return
   */
  private def isVmOverloaded(vm: Vm) = vm.getCpuPercentUtilization > config.getLong("vm.HorizontalScaling.OverLoadThreshold")

}


