# Hyperplane-Hash

A Java implementation of point-to-hyperplane search.

* Bilinear Hyperplane(BH) Neighbor Search
* Embedding Hyperplane(EH) Neighbor Search
* Multilinear Hyperplane(MH) Neighbor Search
* Nearest Hyperplane(NH) Neighbor Search
* Furthest Hyperplane(FH) Neighbor Search

Hyperplane Hash is a technique for hashing multidimensional vectors through linear transformation.
This implementation follows the approach of point-to-hyperplane search to partition data into the dimension space in neighborhoods.

## Why we need nearest neighbor search ?
- Approximate near-neighbor search(NNS)
  - For low-dimensional points, spatial decomposition and tree based search algorithms can provide the exact neighbors in sub-linear time. [1](####ref1)
  - The higher the dimensional data, the more computing cost is required to search. Accordingly, a method for finding a point having a small euclidean distance within a linear time is required.

- Margin-based active learning
  - [Active classifier learning](https://en.wikipedia.org/wiki/Active_learning_(machine_learning)) methods for pool-based selection
    generally scan all database instances before selecting which to have labeled next. [2](####ref2)
  - NNS is useful in large-scale active learning with SVM, maximum margin clustering, and large-margin dimensionality reduction.

## Performance
- BH and MH have been proved to outperform EH. [3](####ref3)
- If the data is normalized, BH and MH also show good enough performance.
- NH and FH search faster than BH and MH, but require more computing power.

## Download

Using maven:
```
<dependency>
    <groupId>io.github.stepping1st</groupId>
    <artifactId>hyperplane-hash</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Examples

```java
import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import io.github.stepping1st.hh.hash.BHHash;
import io.github.stepping1st.hh.search.HashSearch;

import java.util.List;


public class RunHash {

    public static void main(String[] args) {
        double[][] data; // load real data array
        double[] q = new double[]{0.13032633, 0.6227648, -0.33633736, 0.37559462, -0.29248887};
        int dim = q.length;
        // single hasher of the compond hasher
        int m = 4;
        // hash tables
        int l = 4;
        // top n search
        int top = 100;
        // candidate search limit
        int limit = 10000;

        JDKRandomGenerator rg = new JDKRandomGenerator();
        RandomData rd = new RandomDataImpl(rg);

        // generate hash algorithm
        BHHash hash = new BHHash(dim, m, l, rd);
        HashBucket bucket = new HashBucket(dim, l);
        // generate searcher
        HashSearch search = new HashSearch(hash, data, bucket);
        Query query = new Query(q, data, top, limit, Dist.ABS_DOT);

        // return search index of data and distance from query
        List<IdxVal> found = search.nns(query);
        for (IdxVal idx : found) {
            // get data from index
            double[] ds = data[idx.idx()];
        }
    }
}
```
[Check the examples](src/test/java/io/github/stepping1st/hh/RunExample.java)

## Papers
Embedding Hyperplane(EH) Neighbor Search
- [Prateek Jain, Sudheendra Vijayanarasimhan, and Kristen Grauman. 2010. Hashing hyperplane queries to near points with applications to large-scale active learning. In NeurIPS.](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.185.4684&rep=rep1&type=pdf)

Bilinear Hyperplane(BH) Neighbor Search
- [Wei Liu, JunWang, Yadong Mu, Sanjiv Kumar, and Shih-Fu Chang. 2012. Compact hyperplane hashing with bilinear functions. In ICML.](https://icml.cc/Conferences/2012/papers/16.pdf)

Multilinear Hyperplane(MH) Neighbor Search
- [Xianglong Liu, Xinjie Fan, Cheng Deng, Zhujin Li, Hao Su, and Dacheng Tao. 2016. Multilinear hyperplane hashing. In CVPR.](https://openaccess.thecvf.com/content_cvpr_2016/papers/Liu_Multilinear_Hyperplane_Hashing_CVPR_2016_paper.pdf)

Nearest Hyperplane(NH), Furthest Hyperplane(FH) Neighbor Search
- [Point-to-Hyperplane Nearest Neighbor Search Beyond the Unit Hypersphere in SIGMOD 2021.](https://dl.acm.org/doi/pdf/10.1145/3448016.3457240)


## Reference
source code is originally based on https://github.com/HuangQiang/P2HNNS

#### ref1
- J. Freidman, J. Bentley, and A. Finkel. An Algorithm for Finding Best Matches in Logarithmic Expected
  Time. ACM Transactions on Mathematical Software, 3(3):209–226, September 1977.

#### ref2
- Prateek Jain, Sudheendra Vijayanarasimhan, and Kristen Grauman. 2010. Hashing hyperplane queries to near points with applications to large-scale active learning. In NeurIPS.

#### ref3
- Prateek Jain, Sudheendra Vijayanarasimhan, and Kristen Grauman. 2010. Hashing
hyperplane queries to near points with applications to large-scale active learning.
In NeurIPS. 928–936.