package tachyon.worker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import tachyon.conf.WorkerConf;
import tachyon.thrift.BlockInfoException;
import tachyon.thrift.BlockRequest;
import tachyon.thrift.BlockResponse;
import tachyon.thrift.DataService;
import tachyon.thrift.FileDoesNotExistException;
import tachyon.thrift.TachyonException;
import tachyon.util.CommonUtils;

import com.google.common.io.Closer;

public final class DataServiceHandler implements DataService.Iface {
  private static final Logger LOG = Logger.getLogger(DataServiceHandler.class.getName());

  private final BlocksLocker blocksLocker;

  public DataServiceHandler(final BlocksLocker blocksLocker) {
    this.blocksLocker = blocksLocker;
  }

  @Override
  public BlockResponse requestBlock(BlockRequest request) throws BlockInfoException,
      FileDoesNotExistException, TachyonException, TException {
    int lockId = blocksLocker.lock(request.getBlockId());

    try {
      final BlockResponse response = new BlockResponse();

      final long blockId = request.blockId;
      final long offset = request.offset;
      final long len = request.len;

      if (offset < 0) {
        throw new BlockInfoException("Offset can not be negative: " + offset);
      }
      if (len < 0 && len != -1) {
        throw new BlockInfoException("Length can not be negative except -1: " + len);
      }

      String filePath = CommonUtils.concat(WorkerConf.get().DATA_FOLDER, blockId);
      LOG.info("Try to response remote requst by reading from " + filePath);
      final Closer closer = Closer.create();
      try {
        final RandomAccessFile file = closer.register(new RandomAccessFile(filePath, "r"));

        long fileLength = file.length();
        if (offset > fileLength) {
          final String msg =
              String.format("Offset(%d) is larger than file length(%d)", offset, fileLength);
          throw new BlockInfoException(msg);
        }
        if (len != -1 && offset + len > fileLength) {
          final String msg =
              String.format("Offset(%d) plus length(%d) is larger than file length(%d)", offset,
                  len, fileLength);
          throw new BlockInfoException(msg);
        }

        final long readLen;
        if (len == -1) {
          readLen = fileLength - offset;
        } else {
          readLen = len;
        }

        response.setBlockId(blockId);
        response.setOffset(offset);
        response.setLen(readLen);

        FileChannel channel = closer.register(file.getChannel());
        response.setData(channel.map(FileChannel.MapMode.READ_ONLY, offset, readLen));
        LOG.info("Response remote requst by reading from " + filePath + " preparation done.");
      } catch (FileNotFoundException e) {
        throw new FileDoesNotExistException(e.getMessage());
      } catch (IOException e) {
        throw new TachyonException(e.getMessage());
      } finally {
        try {
          closer.close();
        } catch (IOException e) {
          throw new TachyonException(e.getMessage());
        }
      }

      return response;
    } finally {
      blocksLocker.unlock(Math.abs(request.getBlockId()), lockId);
    }
  }
}
