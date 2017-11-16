import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Knapsack {
    private static final int mutationPercent = 4;

    private static final int populationSize = 16;

    private static final int maxGeneration = 7;

    private static final int maxIteration = 50;

    private static final double totalWeight = 5000;

    private static List<Data> items = new ArrayList<>();

    boolean block = false;

    private int crossoverCount = 0;

    private int cloneCount = 0;

    private int breed = 1;

    private double generationFitness = 0;

    private ArrayList<Double> fitness = new ArrayList<>();

    private ArrayList<Double> bestFitnessOfGeneration = new ArrayList<>();

    private ArrayList<Boolean[]> population = new ArrayList<>();

    private ArrayList<Boolean[]> breedPopulation = new ArrayList<>();

    private ArrayList<Boolean[]> bestGeneInGeneration = new ArrayList<>();

    Knapsack() {
        initialize();
        makePopulation();
        if (!block) {
            quantifyPopulation();

            bestGeneInGeneration.add(population.get(findBestSolutionIndex()));

            bestFitnessOfGeneration.add(quantifyGene(population.get(findBestSolutionIndex())));
            System.out.println("BEST GENE IN GENERATION : ");
            System.out.println(printResult(bestGeneInGeneration));
            System.out.println("WINNER");
            bestFitnessOfGeneration.sort(Comparator.naturalOrder());

            if (maxGeneration > 1) {
                makeFurtherGenerations();
            }
        }
    }

    public static void main(String[] args) {
        new Knapsack();

    }

    private void makePopulation() {
        int counter = 0;
        int terminate = 0;
        while (counter < populationSize && terminate < maxIteration) {
            Boolean[] candidate = makeGene();
            if (quantifyGene(candidate) != -1) {
                population.add(candidate);
                counter++;
            } else {
                terminate++;
            }
        }
        if (terminate >= maxIteration) {
            block = true;
            System.out.println("Please restart. MaxIteration number reached.");
        }
    }

    private Boolean[] makeGene() {
        Boolean[] gene = new Boolean[items.size()];
        for (int i = 0; i < gene.length; i++) {
            double random = Math.random();
            if (random > 0.5) {
                gene[i] = true;
            } else {
                gene[i] = false;
            }
        }
        return gene;
    }

    private void quantifyPopulation() {
        generationFitness = 0;
        for (int i = 0; i < populationSize; i++) {
            double currentFitness = quantifyGene(population.get(i));
            fitness.add(currentFitness);
            generationFitness += currentFitness;
        }
    }

    private double quantifyGene(Boolean[] gene) {
        int weight = 0;
        int value = 0;
        for (int i = 0; i < gene.length; i++) {
            if (gene[i]) {
                weight += items.get(i).weight;
                value += items.get(i).value;
            }
        }
        if (weight <= totalWeight) {
            return value;
        }
        return -1;
    }

    private int findBestSolutionIndex() {
        int index = 0;
        double fitness = 0;
        double bestFitness = 0;
        for (int i = 0; i < populationSize; i++) {
            fitness = quantifyGene(population.get(i));
            if (fitness > bestFitness) {
                bestFitness = fitness;
                index = i;
            }
        }
        return index;
    }

    private void breedPopulation() {
        int parentOneIndex;
        int parentTwoIndex;
        breed = breed + 1;
        // If population_size is odd - get best solution of previous generation
        if (populationSize % 2 == 1) {
            breedPopulation.add(bestGeneInGeneration.get(breed - 1));
        }
        parentOneIndex = ThreadLocalRandom.current().nextInt(0, fitness.size());
        parentTwoIndex = ThreadLocalRandom.current().nextInt(0, fitness.size());

        crossoverGenes(parentOneIndex, parentTwoIndex);
    }

    private void crossoverGenes(int parentOneIndex, int parentTwoIndex) {
        Boolean[] childOne = new Boolean[items.size()];
        Arrays.fill(childOne, Boolean.FALSE);

        Boolean[] childTwo = new Boolean[items.size()];
        Arrays.fill(childTwo, Boolean.FALSE);

        double random = Math.random();
        int terminate = 0;
        int counter = 0;
        double prob_crossover = 0.5;
        while (random <= prob_crossover && terminate < maxIteration && counter < populationSize) {
            crossoverCount = crossoverCount + 1;
            int crossPoint = ThreadLocalRandom.current().nextInt(0, items.size());

            for (int i = 0; i < crossPoint; i++) {
                childOne[i] = population.get(parentOneIndex)[i];
                childTwo[i] = population.get(parentOneIndex)[i];
            }
            for (int i = crossPoint; i < items.size(); i++) {
                childOne[i] = population.get(parentOneIndex)[i];
                childTwo[i] = population.get(parentTwoIndex)[i];
            }
            childOne = mutateGene(childOne);
            childTwo = mutateGene(childTwo);

            if (quantifyGene(childOne) != -1) {
                breedPopulation.add(childOne);
                counter++;
            } else {
                terminate++;
            }
            if (quantifyGene(childTwo) != -1) {
                breedPopulation.add(childTwo);
                counter++;
            } else {
                terminate++;
            }
        }
        if (terminate >= maxIteration) {
            block = true;
            return;
        }
        terminate = 0;
        while (random > prob_crossover && terminate < maxIteration && counter < populationSize) {
            cloneCount = cloneCount + 1;

            childOne = mutateGene(childOne);
            childTwo = mutateGene(childTwo);

            if (quantifyGene(childOne) != -1) {
                breedPopulation.add(childOne);
            } else {
                terminate++;
            }
            if (quantifyGene(childTwo) != -1) {
                breedPopulation.add(childTwo);
                break;
            } else {
                terminate++;
            }
        }
        if (terminate >= maxIteration) {
            block = true;
        }
    }

    private Boolean[] mutateGene(Boolean[] geneWithNoMutation) {
        Boolean[] geneWithMutation = Arrays.copyOf(geneWithNoMutation, geneWithNoMutation.length);
        for (int i = 0; i < mutationPercent; i++) {
            int mut_point = ThreadLocalRandom.current().nextInt(0, items.size());
            if (geneWithMutation[mut_point]) {
                geneWithMutation[mut_point] = false;
            } else {
                geneWithMutation[mut_point] = true;
            }
        }
        return geneWithMutation;
    }

    private void makeFurtherGenerations() {
        for (int i = 1; i < maxGeneration; i++) {
            crossoverCount = 0;
            cloneCount = 0;
            for (int j = 0; j < populationSize / 2; j++) {
                breedPopulation();
            }
            fitness.clear();
            quantifyBreedPopulation();

            for (int k = 0; k < populationSize; k++) {
                if (k < breedPopulation.size()) {
                    population.set(k, breedPopulation.get(k));
                }
            }
            breedPopulation.clear();

            bestGeneInGeneration.add(population.get(findBestSolutionIndex()));
            bestFitnessOfGeneration.add(quantifyGene(population.get(findBestSolutionIndex())));

            System.out.println(printResult(bestGeneInGeneration));

            System.out.println();
        }
    }

    private void quantifyBreedPopulation() {
        generationFitness = 0;
        for (int i = 0; i < populationSize; i++) {
            if (i < breedPopulation.size()) {
                double temp_fitness = quantifyGene(breedPopulation.get(i));
                fitness.add(temp_fitness);
            } else {
                //  fitness.add(-100.0);
            }
        }
    }

    private void initialize() {
        items.add(new Data("map", 90, 150));
        items.add(new Data("compass", 130, 35));
        items.add(new Data("water", 1530, 200));
        items.add(new Data("sandwich", 500, 160));
        items.add(new Data("glucose", 150, 60));
        items.add(new Data("tin", 680, 45));
        items.add(new Data("banana", 270, 60));
        items.add(new Data("apple", 390, 40));
        items.add(new Data("cheese", 230, 30));
        items.add(new Data("beer", 520, 10));
        items.add(new Data("suntan cream", 110, 70));
        items.add(new Data("camera", 320, 30));
        items.add(new Data("tshirt", 240, 15));
        items.add(new Data("trousers", 480, 10));
        items.add(new Data("umbrella", 730, 40));
        items.add(new Data("waterproof trousers", 420, 70));
        items.add(new Data("waterproof overclothes", 430, 75));
        items.add(new Data("note-case", 220, 80));
        items.add(new Data("sunglasses", 70, 20));
        items.add(new Data("towel", 180, 120));
        items.add(new Data("socks", 40, 50));
        items.add(new Data("book", 300, 10));
        items.add(new Data("notebook", 900, 1));
        items.add(new Data("tent", 2000, 150));

    }

    private String printResult(List<Boolean[]> generation) {
        StringBuilder sb = new StringBuilder();
        Collections.max(bestFitnessOfGeneration);
        for (Boolean[] gene : generation) {
            int weight = 0;
            int value = 0;
            for (int i = 0; i < gene.length; i++) {
                if (gene[i]) {
                    sb.append("1");
                    weight += items.get(i).weight;
                    value += items.get(i).value;
                } else {
                    sb.append("0");
                }
            }
            sb.append("\tWEIGHT " + weight);
            sb.append("\tVALUE " + value);
            sb.append("\n");
        }
        return sb.toString();
    }

    private static class Data {
        int weight;

        String name;

        int value;

        Data(String name, int weight, int value) {
            this.name = name;
            this.value = value;
            this.weight = weight;
        }
    }

}
