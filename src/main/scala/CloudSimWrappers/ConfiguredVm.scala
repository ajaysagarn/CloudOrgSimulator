package CloudSimWrappers

import HelperUtils.CreateLogger
import Simulations.StartSimulation
import com.typesafe.config.Config
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.core.Simulation
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.resources.{Processor, Ram}
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudsimplus.autoscaling.resources.ResourceScalingInstantaneous
import org.cloudsimplus.autoscaling.{HorizontalVmScaling, HorizontalVmScalingSimple, VerticalVmScaling, VerticalVmScalingSimple}
import org.cloudsimplus.listeners.EventInfo

import collection.JavaConverters.*
import scala.::

class ConfiguredVm(simulation: Simulation,broker: DatacenterBroker, config: Config) {

  val logger = CreateLogger(classOf[ConfiguredVm])
  val vms:List[VmSimple] = createVms()

  val vmTimeLogs: List[String] = List.empty[String]

  def createVms(): List[VmSimple] = {
    val vms = List.fill(config.getInt("vm.InitialCount"))(new VmSimple(config.getDouble("vm.mipsCapacity"), config.getLong("vm.Pes")))
    vms.foreach(vm =>{
      vm.setRam(config.getInt("vm.RAMInMBs"))
      vm.setBw(config.getInt("vm.BandwidthInMBps"))
      vm.setSize(config.getInt("vm.StorageInMMapBs"))
      vm.setSubmissionDelay(config.getInt("vm.DefaultSubmissionDelay"))

      if(config.getBoolean("vm.VerticalCpuScalingEnabled")){
        vm.setPeVerticalScaling(this.createVerticalPeScaling)
      }
      val value = config.getBoolean("vm.VerticalRamScalingEnabled")
      if(config.getBoolean("vm.VerticalRamScalingEnabled")) {
        vm.setRamVerticalScaling(this.createVerticalRamScaling)
      }
    })

    if( config.getBoolean("vm.VerticalCpuScalingEnabled") || config.getBoolean("vm.VerticalCpuScalingEnabled")) {
      simulation.addOnClockTickListener(this.onClockTickListener)
    }

    vms
  }

  def getVms: List[VmSimple] = vms

  def getVmTimeLogs: List[String] = vmTimeLogs


  def submitinitialVms(): Unit ={
    broker.submitVmList(vms.asJava)
  }



  def onClockTickListener(evt: EventInfo): Unit = {
    //perform some logging here to print current vm and host utilization metrics

    vms.foreach(vm => {
      val time = evt.getTime
      val vmid = vm.getId
      val cpuUtil = vm.getCpuPercentUtilization * 100.0
      val noPes = vm.getNumberOfPes
      val cloudlets = vm.getCloudletScheduler.getCloudletExecList.size
      val ramutilPercent = vm.getRam.getPercentUtilization * 100
      val ramAllocation = vm.getRam.getAllocatedResource

      val log = f"Time $time: Vm $vmid CPU Usage: $cpuUtil%% ($noPes vCPUs. Running Cloudlets: #$cloudlets). RAM usage: $ramutilPercent%%  ($ramAllocation MB) \n"

      addVmTimeLog(log)

    })
    //vms.foreach( (vm:VmSimple) => logger.info("\t\tTime {}: Vm {} CPU Usage: {}%% ({} vCPUs. Running Cloudlets: #{}). RAM usage: {}%% ({} MB)%n", evt.getTime, vm.getId, vm.getCpuPercentUtilization * 100.0, vm.getNumberOfPes, vm.getCloudletScheduler.getCloudletExecList.size, vm.getRam.getPercentUtilization * 100, vm.getRam.getAllocatedResource))
  }

  def addVmTimeLog(log:String): Unit = {
    vmTimeLogs ::: List(log)
  }

  private def createVerticalPeScaling = { //The percentage in which the number of PEs has to be scaled
    val scalingFactor = config.getLong("vm.VerticalCpuScaling.ScalingFactor")
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
    val scalingFactor = config.getLong("vm.VerticalRamScaling.ScalingFactor")
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
