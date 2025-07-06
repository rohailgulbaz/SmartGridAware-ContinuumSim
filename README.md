# Smart Grid-Aware Edge-Cloud Continuum Simulation

This repository contains the simulation code for a research study on a Smart Grid-Aware Edge-Cloud Continuum model. The simulation is designed to reflect the complex nature of the continuum environment and its associated stakeholders. The work demonstrates how this model can be applied through a series of experiments.

> 📘 The associated research paper will be linked here once published.

---

## 📂 Project Structure

- **`src/org/cloudbus/cloudsim/`** – Contains modified CloudSim v4.0 classes.
- **`src/my_package/`** – Includes all custom classes developed for the simulation.
- **`jars/`** – Contains required dependencies from CloudSim.
- **`Logs/`** – Contains six pre-populated CSV log files. These logs are updated every time the simulation runs. They are used in the paper to extract evaluation metrics through mathematical modelling. The detailed mathematical modelling part is available in the paper.

---

## 🚀 How to Run

Before running the simulation, make sure to provide the required datasets as referenced below:

1. **Azure Traces**  
   Prepare the Azure traces following the guidelines provided in the paper.  
   [Azure Dataset Info](https://github.com/Azure/AzurePublicDataset/blob/master/AzureFunctionsInvocationTrace2021.md)

2. **Electricity Maps**  
   Download the necessary datasets from the following portal:  
   [Electricity Maps Datasets](https://portal.electricitymaps.com/datasets)

> ⚠️ **Note:** The datasets are **not included** in this repository. When using them, make sure to comply with their respective licenses and attribution requirements.

---

## 📜 License

This project includes and modifies classes from CloudSim v4.0, which is licensed under the [GNU General Public License (GPL)](http://www.gnu.org/copyleft/gpl.html) as mentioned in the headers of each class of original CloudSim.  
As such, this repository also complies with the GPL license terms.

---

## 📌 Citation

If you use this simulation or build upon it, please cite the accompanying research publication once available.
