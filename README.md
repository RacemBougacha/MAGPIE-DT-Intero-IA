# An AI-Augmented MBSE Toolchain for Ontology-Driven Digital Twin Interoperability

**Authors:** Racem Bougacha and Slim Kallel  
**Demonstration:** CoopIS-DT 2026  
**Research area:** Model-Based Systems Engineering (MBSE), Digital Twins, Generative AI, SysML, and Ontology Alignment

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java 17](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)

## Overview

This repository provides the source code and demonstration resources for an AI-augmented Model-Based Systems Engineering (MBSE) toolchain designed to support semantic interoperability between heterogeneous Digital Twin (DT) systems.

The toolchain connects natural-language system descriptions to formal SysML models and OWL ontologies. It then aligns ontologies from different systems to identify semantic correspondences and produce a unified representation.

The demonstration uses two Digital Twin subsystems from the European MAGPIE project:

- **VTMS — Vessel Traffic Monitoring System**
- **PEDM — Port Equipment Digital Maintenance**

The objective is to demonstrate an end-to-end workflow from textual specifications to ontology-based integration.

## Toolchain Architecture

The toolchain consists of three sequential modules.

### Module 1 — Natural Language to SysML

A custom Java desktop application allows users to enter a natural-language system description and select the SysML diagram types to generate.

The module uses **LLaMA 3**, served locally through Ollama, to generate PlantUML code. The generated code is rendered as a diagram preview and exported as XMI for import into Eclipse Papyrus.

The supported SysML v1 diagram types are:

- Block Definition Diagram (BDD)
- State Machine Diagram
- Activity Diagram
- Sequence Diagram

**Interface note:** The current graphical interface is in French.

### Module 2 — SysML to OWL Transformation

This standalone Java module processes the SysML/XMI model and applies rule-based mappings to generate an OWL ontology.

Examples of the mapping approach include representing SysML blocks as OWL classes and associations as OWL object properties.

The module runs separately and does not have a dedicated graphical interface.

### Module 3 — Ontology Alignment and Refactoring

This standalone Java module takes two OWL ontologies as input and applies five complementary matching strategies:

1. Exact-name matching
2. Edit-distance similarity
3. Substring matching
4. Structural matching
5. WordNet-based semantic similarity

For string-based matching, the implementation uses the SMOA distance with a similarity threshold of 0.85.

The module produces an alignment report and a unified, refactored ontology using the AROM refactoring step.

### Inspecting the Results

The generated and aligned ontologies can be opened in **Protégé** to explore the class hierarchy and ontology axioms. Protégé's built-in **HermiT reasoner** is used to check logical consistency, and SPARQL queries can be used to explore information across the integrated ontology.

## Workflow at a Glance

```text
Natural-language system description
                 |
                 v
        LLaMA 3 via Ollama
                 |
                 v
       PlantUML code and preview
                 |
                 v
       SysML/XMI export to Papyrus
                 |
                 v
          SysML-to-OWL
                 |
                 v
       VTMS and PEDM ontologies
                 |
                 v
         Ontology alignment
                 |
                 v
      Unified, refactored ontology
                 |
                 v
     Protégé, HermiT and SPARQL
```

## Evaluation Highlights

The associated research evaluated the toolchain on the VTMS and PEDM use cases.

### Natural Language to SysML

| Metric | VTMS | PEDM |
|---|---:|---:|
| Syntactic validity | 92% | 89% |
| Structural completeness | 0.87 | 0.83 |
| Semantic correctness (out of 5) | 4.2 | 4.0 |

### SysML to OWL

| Metric | VTMS | PEDM |
|---|---:|---:|
| OWL classes generated | 134 | 118 |
| OWL object properties | 52 | 47 |
| Logical consistency | Yes | Yes |

### Ontology Alignment

| Method | Precision | Recall | F1-score |
|---|---:|---:|---:|
| Exact-name matching | 0.62 | 0.45 | 0.52 |
| Edit-distance matching | 0.70 | 0.61 | 0.65 |
| Proposed multi-strategy approach | 0.81 | 0.74 | 0.77 |

The proposed alignment approach achieved an F1-score of **0.77**, compared with 0.65 for the edit-distance baseline.

These figures summarize the evaluation reported in the associated research paper; they should not be interpreted as a guarantee of performance on arbitrary systems or domains.

## Requirements

The documented technology stack includes:

- **Java 17** — Java-based modules
- **Ollama and LLaMA 3** — local language-model inference
- **Eclipse Papyrus** — SysML model inspection
- **Protégé** — OWL ontology inspection and reasoning

The toolchain uses a local Ollama service for LLaMA 3 inference. The other processing stages are implemented in Java.

## Running the Demonstration

The workflow is organized into the following stages:

1. **Prepare the environment.** Install the required Java, Ollama, LLaMA 3, Eclipse Papyrus, and Protégé components.
2. **Generate a SysML model.** Launch the Java desktop application, enter a natural-language description, select the supported diagram types, and inspect the generated preview.
3. **Export and inspect the model.** Export the generated model to XMI and import it into Eclipse Papyrus.
4. **Generate an ontology.** Run the standalone SysML-to-OWL transformation module on the exported model.
5. **Align the ontologies.** Run the standalone alignment module with the two OWL ontology inputs.
6. **Inspect the result.** Open the resulting ontology in Protégé and use HermiT to check logical consistency. SPARQL queries can be used to explore the integrated representation.

**Reproducibility note:** Consult the repository's source files and project configuration for the exact launch procedure and input/output paths of each module. The steps above describe the workflow rather than guaranteed command-line invocations.

## Known Limitations

- **Diagram coverage:** The current implementation supports four selected SysML v1 diagram types.
- **Conversion coverage:** The PlantUML-to-UML conversion supports a subset of notations. Unsupported constructs may be skipped.
- **User interface:** Only the first module has a dedicated graphical interface; the transformation and alignment modules run separately.
- **Evaluation scope:** The evaluation covers two MAGPIE subsystems in the port-logistics domain. The reported results constitute an initial validation rather than a statistically robust assessment.
- **Evaluation methodology:** The associated study uses author-defined reference models and two raters without reported inter-rater agreement. The LLM configuration did not fix a random seed or temperature. Repeated auxiliary concepts may also make some correspondences easier to identify.

Further evaluation with independent annotators, additional domains, and broader model coverage is needed.

## Ongoing Work

Future directions include extending the toolchain to additional SysML elements and Digital Twin domains, and investigating integration with Industry 4.0 standards such as the Asset Administration Shell (AAS) and RAMI 4.0.

Related ongoing work explores AI-assisted interaction with SysML v2 models through Model Context Protocol (MCP) and tools such as SysON. This is a separate development direction and is **not part of the evaluated toolchain presented in this demonstration**.

## Demonstration Video

Watch the toolchain demonstration:

[Open the demonstration video on Google Drive](https://drive.google.com/file/d/1j2n42P4EbQiYiRpthsEgKtJLfCmz2lTz/view)

## Source Code

GitHub repository:

https://github.com/RacemBougacha/MAGPIE-DT-Intero-IA

Please consult the repository for the current source code, example models, datasets, and available demonstration artifacts.

## Related Publication

For the detailed methodology and experimental evaluation, please refer to the companion research paper:

**Bougacha, R., and Kallel, S.** *AI-Augmented MBSE for Ontology-Driven Digital Twin Systems Interoperability.* In: International Conference on Advanced Information Networking and Applications (AINA 2026), Springer Nature Switzerland, pp. 224–236.

```bibtex
@inproceedings{bougacha2026ai,
  title={AI-Augmented MBSE for Ontology-Driven Digital Twin Systems Interoperability},
  author={Bougacha, Racem and Kallel, Slim},
  booktitle={International Conference on Advanced Information Networking and Applications},
  pages={224--236},
  year={2026},
  organization={Springer Nature Switzerland}
}
```

## Acknowledgments

This work is associated with the **MAGPIE project**, funded by the European Commission under the Horizon 2020 Green Deal calls, grant agreement No. 101036594.

## License

This project is intended to be distributed under the **MIT License**. See the [`LICENSE`](LICENSE) file for the applicable terms.

Before distributing the repository under this license, ensure that the code and included resources are authorized for release under MIT and add the full MIT license text to the repository root.
