package CloudSimWrappers

import HelperUtils.{CloudSimUtils, CreateLogger}
import Simulations.StartSimulation
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

class ConfiguredVm(simulation: Simulation,broker: DatacenterBroker, config: Config) {

  val logger = CreateLogger(classOf[ConfiguredVm])
  var vms:List[NetworkVm] = createVms()

  var vmTimeLogs: List[String] = List.empty[String]

  case class VMLogs(logs:List[String])
  var myVMLogs = VMLogs(List.empty[String])

  def createVms(): List[NetworkVm] = {
    val vms = List.fill(config.getInt("vm.InitialCount"))(new NetworkVm(config.getLong("vm.mipsCapacity"), config.getInt("vm.Pes")))
    vms.foreach(vm =>{
      vm.setRam(config.getInt("vm.RAMInMBs"))
      vm.setBw(config.getInt("vm.BandwidthInMBps"))
      vm.setSize(config.getInt("vm.StorageInMMapBs"))
      vm.setCloudletScheduler(CloudSimUtils.getCloudletScheduler(config.getString("vm.CloudletSchedulerType")))
      vm.setSubmissionDelay(config.getInt("vm.DefaultSubmissionDelay"))

      if(config.hasPath("vm.VerticalCpuScalingEnabled") && config.getBoolean("vm.VerticalCpuScalingEnabled")){
        vm.setPeVerticalScaling(this.createVerticalPeScaling)
      }

      if(config.hasPath("vm.VerticalRamScalingEnabled") && config.getBoolean("vm.VerticalRamScalingEnabled")) {
        vm.setRamVerticalScaling(this.createVerticalRamScaling)
      }

      if(config.hasPath("vm.HorizontalScalingEnabled") && config.getBoolean("vm.HorizontalScalingEnabled")) {
        vm.setHorizontalScaling(this.createHorizontalVmScaling)
      }
    })
    simulation.addOnClockTickListener(this.onClockTickListener)
    vms
  }

  def getVms: List[NetworkVm] = vms

  def getVmTimeLogs: List[String] = myVMLogs.logs


  def submitinitialVms(): Unit ={
    broker.submitVmList(vms.asJava)
  }

  def applyCustomerChanges(custconfig: Config): Unit = {
    if(vms.size < custconfig.getInt("InitialCount")){
      val additionalVms: List[NetworkVm] = List.fill(custconfig.getInt("InitialCount") - vms.size)(createVm)
      vms = vms ::: additionalVms
    }

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
      val newList = vmTimeLogs ::: List(log)
      myVMLogs = myVMLogs.copy(myVMLogs.logs ::: List(log))
      logger.trace(log)
    })
  }

  private def createHorizontalVmScaling = {
    val horizontalScaling = new HorizontalVmScalingSimple
    horizontalScaling.setVmSupplier(() => this.createVm).setOverloadPredicate(this.isVmOverloaded)
    horizontalScaling
  }

  private def createVm = {
    val vm = new NetworkVm(config.getLong("vm.mipsCapacity"), config.getInt("vm.Pes"))
    vm.setRam(config.getInt("vm.RAMInMBs"))
    vm.setBw(config.getInt("vm.BandwidthInMBps"))
    vm.setSize(config.getInt("vm.StorageInMMapBs"))
    vm.setCloudletScheduler(CloudSimUtils.getCloudletScheduler(config.getString("vm.CloudletSchedulerType")))
    vm.setSubmissionDelay(config.getInt("vm.DefaultSubmissionDelay"))
    vm
  }

  private def createVerticalPeScaling = { //The percentage in which the number of PEs has to be scaled
    val scalingFactor = config.getDouble("vm.VerticalCpuScaling.ScalingFactor")
    val verticalCpuScaling = new VerticalVmScalingSimple(classOf[Processor], scalingFactor)

    verticalCpuScaling.setResourceScaling((vs: VerticalVmScaling) => 2 * vs.getScalingFactor * vs.getAllocatedResource)
    verticalCpuScaling.setLowerThresholdFunction(this.lowerCpuUtilizationThreshold)
    verticalCpuScaling.setUpperThresholdFunction(this.upperCpuUtilizationThreshold)
    verticalCpuScaling
  }


  def lowerCpuUtilizationThreshold(vm: Vm): Double = {
    config.getDouble("vm.VerticalCpuScaling.LowerCpuUtilizationThreshold")
  }

  def upperCpuUtilizationThreshold(vm: Vm): Double = {
    config.getDouble("vm.VerticalCpuScaling.UpperCpuUtilizationThreshold")
  }

  private def createVerticalRamScaling = { //The percentage in which the number of PEs has to be scaled
    val scalingFactor = config.getDouble("vm.VerticalRamScaling.ScalingFactor")
    val verticalRamScaling = new VerticalVmScalingSimple(classOf[Ram], scalingFactor)

    /* By uncommenting the line below, you will see that, instead of gradually
         * increasing or decreasing the RAM, when the scaling object detects
         * the RAM usage is above or below the defined thresholds,
         * it will automatically calculate the amount of RAM to add/remove to
         * move the VM from the over or underload condition.
        */
    //verticalRamScaling.setResourceScaling(new ResourceScalingInstantaneous())

    //verticalRamScaling.setResourceScaling((vs: VerticalVmScaling) => 2 * vs.getScalingFactor * vs.getAllocatedResource)
    verticalRamScaling.setLowerThresholdFunction(this.lowerRamUtilizationThreshold)
    verticalRamScaling.setUpperThresholdFunction(this.upperRamUtilizationThreshold)
    verticalRamScaling
  }


  def lowerRamUtilizationThreshold(vm: Vm): Double = {
    config.getDouble("vm.VerticalRamScaling.LowerRamUtilizationThreshold")
  }

  def upperRamUtilizationThreshold(vm: Vm): Double = {
    config.getDouble("vm.VerticalRamScaling.UpperRamUtilizationThreshold")
  }
/*

  private def createHorizontalVmScaling(vm: Vm): Unit = {
    val horizontalScaling = new HorizontalVmScalingSimple
    horizontalScaling.setVmSupplier(this.createVm).setOverloadPredicate(this.isVmOverloaded)
    horizontalScaling
  }

  private def createVm = {

    new VmSimple(id, 1000, 2).setRam(512).setBw(1000).setSize(10000).setCloudletScheduler(new CloudletSchedulerTimeShared)
  }*/

  private def isVmOverloaded(vm: Vm) = vm.getCpuPercentUtilization > config.getLong("vm.HorizontalScaling.OverLoadThreshold")

}


