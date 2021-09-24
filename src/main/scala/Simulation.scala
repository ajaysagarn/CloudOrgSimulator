import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{MixedSimulation, RandomVmAllocationSim}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:
  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =
    logger.info("Constructing a cloud model...")
    MixedSimulation.StartMixedSimulation()
    logger.info("Finished cloud simulation...")

class Simulation