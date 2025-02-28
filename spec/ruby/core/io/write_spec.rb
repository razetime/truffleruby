# -*- encoding: utf-8 -*-
require_relative '../../spec_helper'
require_relative 'fixtures/classes'
require_relative 'shared/write'
require_relative 'shared/binwrite'

describe "IO#write on a file" do
  before :each do
    @filename = tmp("IO_syswrite_file") + $$.to_s
    File.open(@filename, "w") do |file|
      file.write("012345678901234567890123456789")
    end
    @file = File.open(@filename, "r+")
    @readonly_file = File.open(@filename)
  end

  after :each do
    @file.close
    @readonly_file.close
    rm_r @filename
  end

  it "does not check if the file is writable if writing zero bytes" do
    -> { @readonly_file.write("") }.should_not raise_error
  end

  it "returns a length of 0 when writing a blank string" do
    @file.write('').should == 0
  end

  before :each do
    @external = Encoding.default_external
    @internal = Encoding.default_internal

    Encoding.default_external = Encoding::UTF_8
  end

  after :each do
    Encoding.default_external = @external
    Encoding.default_internal = @internal
  end

  it "returns the number of bytes written" do
    @file.write("hellø").should == 6
  end

  it "does not modify the passed argument" do
    File.open(@filename, "w") do |f|
      f.set_encoding(Encoding::IBM437)
      # A character whose codepoint differs between UTF-8 and IBM437
      f.write("ƒ".freeze)
    end

    File.binread(@filename).bytes.should == [159]
  end

  it "uses the encoding from the given option for non-ascii encoding" do
    File.open(@filename, "w", encoding: Encoding::UTF_32LE) do |file|
      file.write("hi").should == 8
    end
    File.binread(@filename).should == "h\u0000\u0000\u0000i\u0000\u0000\u0000"
  end

  it "raises a invalid byte sequence error if invalid bytes are being written" do
    # pack "\xFEhi" to avoid utf-8 conflict
    xFEhi = ([254].pack('C*') + 'hi').force_encoding('utf-8')
    File.open(@filename, "w", encoding: Encoding::US_ASCII) do |file|
      -> { file.write(xFEhi) }.should raise_error(Encoding::InvalidByteSequenceError)
    end
  end

  it "writes binary data if no encoding is given" do
    File.open(@filename, "w") do |file|
      file.write('Hëllö'.encode('ISO-8859-1'))
    end
    ë = ([235].pack('U')).encode('ISO-8859-1')
    ö = ([246].pack('U')).encode('ISO-8859-1')
    res = "H#{ë}ll#{ö}"
    File.binread(@filename).should == res.force_encoding(Encoding::BINARY)
  end
end

describe "IO.write" do
  it_behaves_like :io_binwrite, :write

  it "uses an :open_args option" do
    IO.write(@filename, 'hi', open_args: ["w", nil, {encoding: Encoding::UTF_32LE}]).should == 8
  end

  it "disregards other options if :open_args is given" do
    IO.write(@filename, 'hi', 2, mode: "r", encoding: Encoding::UTF_32LE, open_args: ["w"]).should == 2
    File.read(@filename).should == "\0\0hi"
  end

  it "uses the given encoding and returns the number of bytes written" do
    IO.write(@filename, 'hi', mode: "w", encoding: Encoding::UTF_32LE).should == 8
  end

  it "writes the file with the permissions in the :perm parameter" do
    rm_r @filename
    IO.write(@filename, 'write :perm spec', mode: "w", perm: 0o755).should == 16
    (File.stat(@filename).mode & 0o777) == 0o755
  end

  it "writes binary data if no encoding is given" do
    IO.write(@filename, 'Hëllö'.encode('ISO-8859-1'))
    xEB = [235].pack('C*')
    xF6 = [246].pack('C*')
    File.binread(@filename).should == ("H" + xEB + "ll" + xF6).force_encoding(Encoding::BINARY)
  end

  platform_is_not :windows do
    describe "on a FIFO" do
      before :each do
        @fifo = tmp("File_open_fifo")
        File.mkfifo(@fifo)
      end

      after :each do
        rm_r @fifo
      end

      it "writes correctly" do
        thr = Thread.new do
          IO.read(@fifo)
        end
        begin
          string = "hi"
          IO.write(@fifo, string).should == string.length
        ensure
          thr.join
        end
      end
    end
  end
end

describe "IO#write" do
  it_behaves_like :io_write, :write

  it "accepts multiple arguments" do
    IO.pipe do |r, w|
      w.write("foo", "bar")
      w.close

      r.read.should == "foobar"
    end
  end
end

platform_is :windows do
  describe "IO#write on Windows" do
    before :each do
      @fname = tmp("io_write.txt")
    end

    after :each do
      rm_r @fname
      @io.close if @io and !@io.closed?
    end

    it "normalizes line endings in text mode" do
      @io = new_io(@fname, "wt")
      @io.write "a\nb\nc"
      @io.close
      File.binread(@fname).should == "a\r\nb\r\nc"
    end

    it "does not normalize line endings in binary mode" do
      @io = new_io(@fname, "wb")
      @io.write "a\r\nb\r\nc"
      @io.close
      File.binread(@fname).should == "a\r\nb\r\nc"
    end
  end
end

ruby_version_is "3.0" do
  describe "IO#write on STDOUT" do
    # https://bugs.ruby-lang.org/issues/14413
    platform_is_not :windows do
      it "raises SignalException SIGPIPE if the stream is closed instead of Errno::EPIPE like other IOs" do
        stderr_file = tmp("stderr")
        begin
          IO.popen([*ruby_exe, "-e", "loop { puts :ok }"], "r", err: stderr_file) do |io|
            io.gets.should == "ok\n"
            io.close
          end
          status = $?
          status.should_not.success?
          status.should.signaled?
          Signal.signame(status.termsig).should == 'PIPE'
          File.read(stderr_file).should.empty?
        ensure
          rm_r stderr_file
        end
      end
    end
  end
end
