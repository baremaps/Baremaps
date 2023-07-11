/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.benchmarks;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.apache.baremaps.database.collection.DataMap;
import org.apache.baremaps.database.collection.Long2ObjectMemoryAlignedDataMap;
import org.apache.baremaps.database.memory.MemoryMappedFile;
import org.apache.baremaps.database.memory.OffHeapMemory;
import org.apache.baremaps.database.memory.OnHeapMemory;
import org.apache.baremaps.database.type.LongDataType;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class MemoryBenchmark {

  private static final long N = 1 << 25;

  private void benchmark(DataMap<Long> map, long n) {
    for (long i = 0; i < n; i++) {
      map.put(i, i);
    }
    for (long i = 0; i < n; i++) {
      long v = map.get(i);
      if (v != i) {
        throw new RuntimeException("Invalid value");
      }
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void onHeap() {
    benchmark(new Long2ObjectMemoryAlignedDataMap<>(new LongDataType(), new OnHeapMemory()), N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void offHeap() {
    benchmark(new Long2ObjectMemoryAlignedDataMap<>(new LongDataType(), new OffHeapMemory()), N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void onDisk() throws IOException {
    Path file = Files.createTempFile(Paths.get("."), "baremaps_", ".tmp");
    benchmark(new Long2ObjectMemoryAlignedDataMap<>(new LongDataType(), new MemoryMappedFile(file)),
        N);
    Files.delete(file);
  }

  public static void main(String[] args) throws RunnerException {
    org.openjdk.jmh.runner.options.Options opt =
        new OptionsBuilder().include(MemoryBenchmark.class.getSimpleName()).forks(1).build();
    new Runner(opt).run();
  }
}
