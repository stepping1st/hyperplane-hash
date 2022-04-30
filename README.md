# Hyperplane-Hash

A Java implementation of point-to-hyperplane search.

* Bilinear Hyperplane(BH) Neighbor Search
* Embedding Hyperplane(EH) Neighbor Search
* Multilinear Hyperplane(MH) Neighbor Search
* Nearest Hyperplane(NH) Neighbor Search
* Furthest Hyperplane(FH) Neighbor Search

Hyperplane Hash is a technique for hashing multidimensional vectors through linear transformation.
This implementation follows the approach of point-to-hyperplane search to partition data into the dimension space in neighborhoods.

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
import org.github.stepping1st.hh.hash.BHHash;
import org.github.stepping1st.hh.search.HashSearch;

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
[Check the examples](src/test/java/org/github/stepping1st/hh/RunExample.java)

## Reference
source code is originally based on https://github.com/HuangQiang/P2HNNS

## Papers
Bilinear Hyperplane(BH) Neighbor Search
- [Wei Liu, JunWang, Yadong Mu, Sanjiv Kumar, and Shih-Fu Chang. 2012. Compact hyperplane hashing with bilinear functions. In ICML.](https://icml.cc/Conferences/2012/papers/16.pdf)

Embedding Hyperplane(EH) Neighbor Search
- [Prateek Jain, Sudheendra Vijayanarasimhan, and Kristen Grauman. 2010. Hashing hyperplane queries to near points with applications to large-scale active learning. In NeurIPS.](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.185.4684&rep=rep1&type=pdf)

Multilinear Hyperplane(MH) Neighbor Search
- [Xianglong Liu, Xinjie Fan, Cheng Deng, Zhujin Li, Hao Su, and Dacheng Tao. 2016. Multilinear hyperplane hashing. In CVPR.](https://openaccess.thecvf.com/content_cvpr_2016/papers/Liu_Multilinear_Hyperplane_Hashing_CVPR_2016_paper.pdf)

Nearest Hyperplane(NH), Furthest Hyperplane(FH) Neighbor Search
- [Point-to-Hyperplane Nearest Neighbor Search Beyond the Unit Hypersphere in SIGMOD 2021.](https://dl.acm.org/doi/pdf/10.1145/3448016.3457240)
